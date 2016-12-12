package com.bg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgreSQLJDBC{
	private Connection c = null;
	
	public void connect() throws Exception{
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager
					.getConnection("jdbc:postgresql://localhost:5432/postgres",
							"postgres", "loveCMSC424");
			c.setAutoCommit(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}
	
	public void insert() throws Exception{
		Statement stmt = null;
		try {
			
			stmt = c.createStatement();
			String sql = "";
			stmt.executeUpdate(sql);
			
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		System.out.println("Records created successfully");
	}
	
	public String reelectionQuery() throws Exception{
		Statement stmt = null;
		String returnString = "<h2>Non-Contiguous Election Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "with CY as (select C.\"CandidateName\", C.\"CandidateID\", PRC.\"Year\" "
					+ "from public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC "
					+ "where C.\"CandidateID\" = PRC.\"CandidateID\" and PRC.\"isPresident\" = true), "
					+ "wins as (select C.\"CandidateName\", C.\"CandidateID\", count(PRC.\"Year\") wincount "
					+ "from public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC "
					+ "where C.\"CandidateID\" = PRC.\"CandidateID\" and PRC.\"isPresident\" = true "
					+ "and PRC.\"Year\" % 4 = 0 group by C.\"CandidateName\", C.\"CandidateID\"), "
					+ "trips as (select CY1.\"CandidateID\" top, CY2.\"CandidateID\" mid, CY3.\"CandidateID\" bot "
					+ "from CY CY1, CY CY2, CY CY3 where CY1.\"Year\" = CY2.\"Year\"+4 "
					+ "and CY2.\"Year\" = CY3.\"Year\"+4) "
					+ "select distinct CY.\"CandidateName\" from trips, wins, CY "
					+ "where wins.\"CandidateID\" = trips.\"mid\" and wins.\"CandidateID\" != trips.\"top\" "
					+ "and wins.\"CandidateID\" != trips.\"bot\" and wins.wincount > 1 and CY.\"CandidateID\" = trips.\"mid\";";
			
			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				
				//Format
				returnString += "NAME: " + c_name + "<br>";
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String candQuery(String cand_name) throws Exception{
		Statement stmt = null;
		cand_name = "'%" + cand_name + "%'";
		String returnString = "<h2>Candidate Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "SELECT C.\"CandidateName\", PRC.\"isPresident\", PRC.\"PartyName\", PRC.\"Year\", ETC.\"ElectoralVotes\", ETC.\"PopularVotes\""
					+ " FROM public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC, public.\"ElectionTallyCandidates\" ETC"
					+ " WHERE C.\"CandidateID\" = PRC.\"CandidateID\" and C.\"CandidateID\" = ETC.\"CandidateID\" and PRC.\"Year\" = ETC.\"Year\" and C.\"CandidateName\" like "
					+ cand_name + "UNION ALL SELECT C.\"CandidateName\", PRC.\"isPresident\", PRC.\"PartyName\", PRC.\"Year\", PRC.\"Year\" ElectoralVotes, PRC.\"Year\" PopularVotes"
					+ " FROM public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC"
					+ " WHERE C.\"CandidateID\" = PRC.\"CandidateID\" and C.\"CandidateName\" like "
					+ cand_name + " and PRC.\"Year\"%4 != 0 and PRC.\"Year\" != 1789 order by \"CandidateName\", \"Year\" desc;";
			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				boolean is_pres = res.getBoolean("ISPRESIDENT");
				String party = res.getString("PARTYNAME");
				int year = res.getInt("YEAR");
				int elec = res.getInt("ELECTORALVOTES");
				float pop = res.getFloat("POPULARVOTES");
				
				if (year != elec){
					//Format
					returnString += "NAME: " + c_name + "<br>";
					returnString += "ELECTION YEAR: " + year + "<br><br>";
					returnString += "ELECTED: " + is_pres + "<br>";
					returnString += "PARTY: " + party + "<br>";
					returnString += "ELECTORAL VOTES: " + elec + "<br>";
					
					//DNE popular vote fix
					if(year < 1825){
						returnString += "POPULAR VOTES: N/A<br>";
					}
					else{
						returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
					}
				}
				//Otherwise president as result of no election
				else{
					returnString += "NAME: " + c_name + "<br>";
					returnString += "YEAR: " + year + "<br><br>";
					returnString += "PARTY: " + party + "<br>";
					returnString += "PRESIDENT AS RESULT OF DEATH/IMPEACHMENT.<br>";
					returnString += "NOT ELECTED.<br>";
				}
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String swingCandQuery() throws Exception{
		Statement stmt = null;
		String returnString = "<h2>Swing Candidate Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "with lst as (select distinct PRC1.\"CandidateID\""
					+ " From public.\"PartyRepresentCandidates\" PRC1, public.\"PartyRepresentCandidates\" PRC2"
					+ " Where PRC1.\"Year\" != PRC2.\"Year\" and PRC1.\"CandidateID\" = PRC2.\"CandidateID\""
					+ " and PRC1.\"PartyName\" not like PRC2.\"PartyName\" and PRC1.\"Year\"%4 = 0 "
					+ " and PRC2.\"Year\"%4 = 0)"
					+ " SELECT C.\"CandidateName\", PRC.\"isPresident\", PRC.\"PartyName\", PRC.\"Year\","
					+ " ETC.\"ElectoralVotes\", ETC.\"PopularVotes\""
					+ " FROM public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC,"
					+ " public.\"ElectionTallyCandidates\" ETC, lst LST"
					+ " WHERE C.\"CandidateID\" = LST.\"CandidateID\" and C.\"CandidateID\" = PRC.\"CandidateID\""
					+ " and C.\"CandidateID\" = ETC.\"CandidateID\" and PRC.\"Year\" = ETC.\"Year\" UNION ALL"
					+ " SELECT C.\"CandidateName\", PRC.\"isPresident\", PRC.\"PartyName\", PRC.\"Year\","
					+ " PRC.\"Year\" ElectoralVotes, PRC.\"Year\" PopularVotes"
					+ " FROM public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC, lst LST"
					+ " WHERE C.\"CandidateID\" = PRC.\"CandidateID\" and C.\"CandidateID\" = LST.\"CandidateID\""
					+ " and PRC.\"Year\"%4 != 0 and PRC.\"Year\" != 1789 order by \"CandidateName\", \"Year\" desc;";

			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				boolean is_pres = res.getBoolean("ISPRESIDENT");
				String party = res.getString("PARTYNAME");
				int year = res.getInt("YEAR");
				int elec = res.getInt("ELECTORALVOTES");
				float pop = res.getFloat("POPULARVOTES");
				
				if (year != elec){
					//Format
					returnString += "NAME: " + c_name + "<br>";
					returnString += "ELECTION YEAR: " + year + "<br><br>";
					returnString += "ELECTED: " + is_pres + "<br>";
					returnString += "PARTY: " + party + "<br>";
					returnString += "ELECTORAL VOTES: " + elec + "<br>";
					
					//DNE popular vote fix
					if(year < 1825){
						returnString += "POPULAR VOTES: N/A<br>";
					}
					else{
						returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
					}
				}
				//Otherwise president as result of no election
				else{
					returnString += "NAME: " + c_name + "<br>";
					returnString += "YEAR: " + year + "<br><br>";
					returnString += "PARTY: " + party + "<br>";
					returnString += "PRESIDENT AS RESULT OF DEATH/IMPEACHMENT.<br>";
					returnString += "NOT ELECTED.<br>";
				}
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String electionYearQuery(String elec_year) throws Exception{
		Statement stmt = null;
		if (elec_year.equals("")){
			elec_year = "0";
		}
		
		String returnString = "<h2>Election of " + elec_year + " Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			//Winner
			stmt = c.createStatement();
			String sql = "select C.\"CandidateName\", PRC.\"PartyName\", E.\"VicePres\", ETC.\"ElectoralVotes\", "
					+ "ETC.\"PopularVotes\" from public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC, "
					+ "public.\"Election\" E, public.\"ElectionTallyCandidates\" ETC where C.\"CandidateID\" = E.\"Winner\" "
					+ "and C.\"CandidateID\" = PRC.\"CandidateID\" and ETC.\"CandidateID\" = C.\"CandidateID\" "
					+ "and PRC.\"Year\" = E.\"Year\" and E.\"Year\" = ETC.\"Year\" and E.\"Year\" = " + elec_year + ";";
			ResultSet res = stmt.executeQuery(sql);

			returnString += "<h3>Winner:</h3>-----------------------------------------<br>";
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				String party = res.getString("PARTYNAME");
				String vice = res.getString("VICEPRES");
				int elec = res.getInt("ELECTORALVOTES");
				float pop = res.getFloat("POPULARVOTES");
				
				//Format
				returnString += "PRESIDENT: " + c_name + "<br><br>";
				returnString += "PARTY: " + party + "<br>";
				returnString += "VICE PRESIDENT: " + vice + "<br>";
				returnString += "ELECTORAL VOTES: " + elec + "<br>";
				
				//DNE popular vote fix
				if(Integer.parseInt(elec_year) < 1825){
					returnString += "POPULAR VOTES: N/A<br>";
				}
				else{
					returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
				}
				
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}

			//Candidates
			if (!no_res){
				stmt = c.createStatement();
				sql = "select C.\"CandidateName\", PRC.\"PartyName\", ETC.\"ElectoralVotes\", "
						+ "ETC.\"PopularVotes\" from public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC, "
						+ "public.\"Election\" E, public.\"ElectionTallyCandidates\" ETC where C.\"CandidateID\" != E.\"Winner\" "
						+ "and C.\"CandidateID\" = PRC.\"CandidateID\" and ETC.\"CandidateID\" = C.\"CandidateID\" "
						+ "and PRC.\"Year\" = E.\"Year\" and E.\"Year\" = ETC.\"Year\" and E.\"Year\" = " + elec_year + ";";
				res = stmt.executeQuery(sql);
				
				returnString += "<h3>Other Candidates:</h3>-----------------------------------------<br>";
				
				//Make results into displayable format
				while(res.next()){
					//Retrieve by column name
					String c_name = res.getString("CANDIDATENAME");
					String party = res.getString("PARTYNAME");
					int elec = res.getInt("ELECTORALVOTES");
					float pop = res.getFloat("POPULARVOTES");
					
					//Format
					returnString += "PRESIDENT: " + c_name + "<br><br>";
					returnString += "PARTY: " + party + "<br>";
					returnString += "ELECTORAL VOTES: " + elec + "<br>";
					
					//DNE popular vote fix
					if(Integer.parseInt(elec_year) < 1825){
						returnString += "POPULAR VOTES: N/A<br>";
					}
					else{
						returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
					}
					
					
					//Separator
					returnString += "-----------------------------------------<br>";
				}
			}
			
			//Polls
			if (!no_res){
				stmt = c.createStatement();
				sql = "select C.\"CandidateName\", P.\"PollName\", P.\"Month\", PSC.\"PopularVote\" "
						+ "from public.\"Candidate\" C, public.\"Polls\" P, public.\"PollsSurveyCandidates\" PSC "
						+ "where C.\"CandidateID\" = PSC.\"CandidateID\" and PSC.\"PollID\" = P.\"PollID\" and P.\"Year\" = "
						+ elec_year + " order by P.\"PollID\";";
				res = stmt.executeQuery(sql);
				
				returnString += "<h3>Polls:</h3>-----------------------------------------<br>";
				
				//Make results into displayable format
				while(res.next()){
					//Retrieve by column name
					String c_name = res.getString("CANDIDATENAME");
					String month = res.getString("MONTH");
					String p_name = res.getString("POLLNAME");
					float pop = res.getFloat("POPULARVOTE");
					
					//Format
					returnString += month + " " + p_name + " Poll:<br>";
					returnString += "Popular votes for " + c_name + ": " + (int)(pop*10000)/100.0 + "%<br>";
					
					
					//Separator
					returnString += "-----------------------------------------<br>";
				}
			}
			
			if (no_res){
				returnString = "<h2>Election of " + elec_year + " Query Results</h2><br>-----------------------------------------<br>";
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String partyQuery(String party_name) throws Exception{
		Statement stmt = null;
		if (party_name.equals("")){
			party_name = "0";
		}
		String returnString = "<h2>Party Historical Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "SELECT C.\"CandidateName\", PRC.\"isPresident\", PRC.\"Year\", ETC.\"ElectoralVotes\", "
					+ "ETC.\"PopularVotes\" FROM public.\"Candidate\" C, public.\"PartyRepresentCandidates\" PRC, "
					+ "public.\"ElectionTallyCandidates\" ETC WHERE C.\"CandidateID\" = PRC.\"CandidateID\" and C.\"CandidateID\" = ETC.\"CandidateID\" "
					+ "and PRC.\"Year\" = ETC.\"Year\" and PRC.\"PartyName\" = '" + party_name + "' order by PRC.\"Year\" desc;";
			ResultSet res = stmt.executeQuery(sql);
			
			returnString += "<h3>" + party_name + " Election History:</h3>-----------------------------------------<br>";
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				boolean is_pres = res.getBoolean("ISPRESIDENT");
				int year = res.getInt("YEAR");
				int elec = res.getInt("ELECTORALVOTES");
				float pop = res.getFloat("POPULARVOTES");
				
				//Format
				returnString += "PARTY REPRESENTER: " + c_name + "<br>";
				returnString += "ELECTION YEAR: " + year + "<br><br>";
				returnString += "ELECTED: " + is_pres + "<br>";
				returnString += "ELECTORAL VOTES: " + elec + "<br>";
				
				//DNE popular vote fix
				if(year < 1825){
					returnString += "POPULAR VOTES: N/A<br>";
				}
				else{
					returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
				}
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			//Statistics
			if (!no_res){
				stmt = c.createStatement();
				sql = "with wt as (SELECT COALESCE((SELECT count(PRC.\"Year\") wins FROM public.\"PartyRepresentCandidates\" PRC "
						+ "WHERE PRC.\"Year\"%4 = 0 and PRC.\"isPresident\" = true and PRC.\"PartyName\" = '"
						+ party_name + "' GROUP BY PRC.\"PartyName\"), 0) wins), "
						+ "lt as (SELECT COALESCE((SELECT count(PRC.\"Year\") losses FROM public.\"PartyRepresentCandidates\" PRC "
						+ "WHERE PRC.\"Year\"%4 = 0 and PRC.\"isPresident\" = false and PRC.\"PartyName\" = '"
						+ party_name + "' GROUP BY PRC.\"PartyName\"), 0) losses), "
						+ "ev as (SELECT COALESCE((SELECT sum(ETC.\"ElectoralVotes\") totalelectoral "
						+ "FROM public.\"PartyRepresentCandidates\" PRC, public.\"ElectionTallyCandidates\" ETC "
						+ "WHERE PRC.\"CandidateID\" = ETC.\"CandidateID\" and PRC.\"Year\" = ETC.\"Year\"  "
						+ "and PRC.\"PartyName\" = '" + party_name + "' GROUP BY PRC.\"PartyName\"), 0) totalelectoral), "
						+ "pv as (SELECT COALESCE((SELECT sum(ETC.\"PopularVotes\") totalpopular "
						+ "FROM public.\"PartyRepresentCandidates\" PRC, public.\"ElectionTallyCandidates\" ETC "
						+ "WHERE PRC.\"CandidateID\" = ETC.\"CandidateID\" and PRC.\"Year\" = ETC.\"Year\" "
						+ "and PRC.\"Year\" > 1824 and PRC.\"PartyName\" = '" + party_name + "' GROUP BY PRC.\"PartyName\"), 0) totalpopular) "
						+ "SELECT * FROM wt, lt, ev, pv;";
				res = stmt.executeQuery(sql);
				
				returnString += "<h3>" + party_name + " Party Statistics:</h3>-----------------------------------------<br>";
				
				//Make results into displayable format
				while(res.next()){
					//Retrieve by column name
					int wins = res.getInt("WINS");
					int losses = res.getInt("LOSSES");
					int elec = res.getInt("TOTALELECTORAL");
					float pop = res.getFloat("TOTALPOPULAR");
					
					//Format
					returnString += "TOTAL ELECTIONS: " + (wins+losses) + "<br>";
					returnString += "WIN PERCENTAGE: " + (int)(((float)wins/(wins+losses))*10000)/100.0 + "%<br>";
					returnString += "LOSS PERCENTAGE: " + (int)(((float)losses/(wins+losses))*10000)/100.0 + "%<br>";
					returnString += "AVG ELECTORAL VOTES: " + (int)(((float)elec/(wins+losses))*100)/100.0 + "<br>";
					
					//DNE popular vote fix
					if(pop > 0.0){
						returnString += "AVG POPULAR VOTES: " + (int)(((float)pop/(wins+losses))*10000)/100.0 + "%<br>";
					}
					else{
						returnString += "AVG POPULAR VOTES: N/A<br>";
					}
					
					//Separator
					returnString += "-----------------------------------------<br>";
				}
			}
			
			if (no_res){
				returnString = "<h2>Party Historical Query Results</h2><br>-----------------------------------------<br>";
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String popularVoteQuery() throws Exception{
		Statement stmt = null;
		String returnString = "<h2>Popular Vote Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "with popgetter as (SELECT * FROM public.\"ElectionTallyCandidates\" ETC3 "
					+ "WHERE \"Year\" > 1820 EXCEPT SELECT ETC1.* FROM public.\"ElectionTallyCandidates\" ETC1, "
					+ "public.\"ElectionTallyCandidates\" ETC2 WHERE ETC1.\"Year\" = ETC2.\"Year\" "
					+ "and ETC1.\"PopularVotes\" < ETC2.\"PopularVotes\") "
					+ "Select c.\"CandidateName\", C2.\"CandidateName\" winner, popgetter.\"Year\", popgetter.\"PopularVotes\", "
					+ "popgetter.\"ElectoralVotes\" From public.\"Election\" E, popgetter, "
					+ "public.\"Candidate\" C, public.\"Candidate\" C2 Where popgetter.\"CandidateID\" = C.\"CandidateID\" "
					+ "and E.\"Winner\" != popgetter.\"CandidateID\" and E.\"Winner\" = C2.\"CandidateID\" and E.\"Year\" = popgetter.\"Year\" "
					+ "order by popgetter.\"Year\" desc;";

			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				String winner = res.getString("WINNER");
				int year = res.getInt("YEAR");
				int elec = res.getInt("ELECTORALVOTES");
				float pop = res.getFloat("POPULARVOTES");
				
				returnString += "NAME: " + c_name + "<br>";
				returnString += "ELECTION YEAR: " + year + "<br><br>";
				returnString += "ELECTORAL VOTES: " + elec + "<br>";
				returnString += "POPULAR VOTES: " + (int)(pop*10000)/100.0 + "%<br>";
				returnString += "BEATEN BY: " + winner + "<br>";
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String swingStateQuery(String year) throws Exception{
		Statement stmt = null;
		if (year.equals("")){
			year = "0";
		}
		String returnString = "<h2>Swing State Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "with excl as (SELECT distinct SVC1.\"StateName\" FROM public.\"StateVoteForCandidates\" SVC1, "
					+ "public.\"StateVoteForCandidates\" SVC2 WHERE SVC1.\"Year\" = SVC2.\"Year\" "
					+ "and SVC1.\"ElectoralVotes\" > SVC2.\"ElectoralVotes\" and SVC1.\"StateName\" = SVC2.\"StateName\" "
					+ "and SVC1.\"Year\" = " + year + " UNION SELECT distinct SVC1.\"StateName\" "
					+ "FROM public.\"StateVoteForCandidates\" SVC1, public.\"StateVoteForCandidates\" SVC2 "
					+ "WHERE SVC1.\"Year\" = SVC2.\"Year\" and SVC1.\"ElectoralVotes\" > SVC2.\"ElectoralVotes\" "
					+ "and SVC1.\"StateName\" = SVC2.\"StateName\" and "
					+ "(SVC1.\"Year\" = " + year + "-4 or SVC1.\"Year\" = " + year + "-3))"
					+ "SELECT distinct SVC1.\"StateName\" FROM public.\"StateVoteForCandidates\" SVC1, "
					+ "public.\"StateVoteForCandidates\" SVC2, public.\"PartyRepresentCandidates\" PRC1, "
					+ "public.\"PartyRepresentCandidates\" PRC2 WHERE SVC1.\"StateName\" like SVC2.\"StateName\" "
					+ "and (SVC1.\"Year\" = SVC2.\"Year\"+4 or SVC1.\"Year\" = SVC2.\"Year\"+3) "
					+ "and SVC1.\"CandidateID\" = PRC1.\"CandidateID\" and PRC1.\"Year\" = SVC1.\"Year\" "
					+ "and SVC2.\"CandidateID\" = PRC2.\"CandidateID\" and PRC2.\"Year\" = SVC2.\"Year\" "
					+ "and PRC1.\"PartyName\" not like PRC2.\"PartyName\" and SVC1.\"Year\" = " + year + " EXCEPT "
					+ "SELECT \"StateName\" FROM excl;";

			ResultSet res = stmt.executeQuery(sql);
			
			returnString += "<h3>States That Completely Changed Party for the " + year + " Election:</h3>-----------------------------------------<br>";
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String s_name = res.getString("STATENAME");
				
				returnString += s_name + "<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			else{
				returnString += "-----------------------------------------<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String electoralVotesQuery(String year) throws Exception{
		Statement stmt = null;
		if (year.equals("")){
			year = "0";
		}
		
		String returnString = "<h2>Electoral Votes for " + year + " Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			//States' votes
			stmt = c.createStatement();
			String sql = "SELECT SVC.\"StateName\", SVC.\"ElectoralVotes\", C.\"CandidateName\" "
					+ "FROM public.\"StateVoteForCandidates\" SVC, public.\"Candidate\" C "
					+ "WHERE SVC.\"Year\" = " + year + " and SVC.\"CandidateID\" = C.\"CandidateID\" "
					+ "order by \"CandidateName\", \"StateName\";";
			ResultSet res = stmt.executeQuery(sql);

			returnString += "<h3>Electoral Votes By State:</h3>-----------------------------------------<br>";
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				int elec = res.getInt("ELECTORALVOTES");
				String s_name = res.getString("STATENAME");
				
				//Format
				returnString += s_name + " voted " + elec + " <br>for " + c_name + "<br>";
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}

			//total electoral votes
			if (!no_res){
				stmt = c.createStatement();
				sql = "SELECT sum(SVC.\"ElectoralVotes\") totalelectoral, C.\"CandidateName\" "
						+ "FROM public.\"StateVoteForCandidates\" SVC, public.\"Candidate\" C "
						+ "WHERE SVC.\"Year\" = " + year + " and SVC.\"CandidateID\" = C.\"CandidateID\" "
						+ "group by \"CandidateName\";";
				res = stmt.executeQuery(sql);
				
				returnString += "<h3>Total Electoral Votes:</h3>-----------------------------------------<br>";
				
				//Make results into displayable format
				while(res.next()){
					//Retrieve by column name
					String c_name = res.getString("CANDIDATENAME");
					int elec = res.getInt("TOTALELECTORAL");
					
					//Format
					returnString += c_name + " received " + elec + " total electoral votes.<br>";
					
					
					//Separator
					returnString += "-----------------------------------------<br>";
				}
			}
			
			if (no_res){
				returnString = "<h2>Electoral Votes for " + year + " Query Results</h2><br>-----------------------------------------<br>";
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public String pollAccuracy(String year) throws Exception{
		Statement stmt = null;
		if (year.equals("")){
			year = "0";
		}
		
		String returnString = "<h2>Poll Accuracy of the " + year + " Election Query Results</h2><br>-----------------------------------------<br>";
		try {
			
			stmt = c.createStatement();
			String sql = "SELECT C.\"CandidateName\", PSC.\"PopularVote\", PSC.\"PopularVote\"-ETC.\"PopularVotes\" deviation, "
					+ "P.\"PollName\", P.\"Month\" FROM public.\"Polls\" P, public.\"PollsSurveyCandidates\" "
					+ "PSC, public.\"ElectionTallyCandidates\" ETC, public.\"Candidate\" C WHERE P.\"Year\" = " + year + " "
					+ "and P.\"PollID\" = PSC.\"PollID\" and PSC.\"CandidateID\" = ETC.\"CandidateID\" and P.\"Year\" = ETC.\"Year\" "
					+ "and C.\"CandidateID\" = PSC.\"CandidateID\" order by P.\"PollID\";";
			ResultSet res = stmt.executeQuery(sql);
			
			//Make results into displayable format
			boolean no_res = true;
			while(res.next()){
				//Retrieve by column name
				String c_name = res.getString("CANDIDATENAME");
				String month = res.getString("MONTH");
				String p_name = res.getString("POLLNAME");
				float pop = res.getFloat("POPULARVOTE");
				float dev = res.getFloat("DEVIATION");
				
				//Format
				returnString += "CANDIDATE: " + c_name + "<br>";
				returnString += "POLL NAME: " + p_name + "<br>";
				returnString += "MONTH: " + month + "<br>";
				returnString += "POLLED POPULAR VOTE: " + (int)(pop*10000)/100.0 + "%<br><br>";
				returnString += "DEVIATION: " + (int)(dev*10000)/100.0 + "%<br>";
				
				//Separator
				returnString += "-----------------------------------------<br>";
				
				no_res = false;
			}
			
			if (no_res){
				returnString += "NO RESULTS<br>";
			}
			
	        res.close();
			stmt.close();
			c.commit();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		
		return returnString;
	}
	
	public void disconnect() throws Exception {
		try{
			c.close();
			
		} catch (Exception e) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
	}
}