<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Presidential Elections Database</title>
</head>
<script type="text/javascript">
function extraInput(val){
	var extraInput=document.getElementById('extradiv');
	if(val=='candidate'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Candidate Name:';
	}
	else if(val=='electionYear'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Year:';
	}
	else if(val=='party'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Party Name:';
	}
	else if(val=='pollAccuracy'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Year:';
	}
	else if(val=='swingState'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Year:';
	}
	else if(val=='electoralVote'){
		extraInput.style.display='block';
		document.getElementById("el").innerHTML = 'Year:';
	}
	else  
		extraInput.style.display='none';
}

</script> 
<body>

<h1 align="center">Welcome to Presidential Elections Database</h1>

<form action="${pageContext.request.contextPath}/QueryBuilder" method="post">

	<p>Select a Query: 
    <select name="queryName" onChange='extraInput(this.value);'>
        <option selected="selected" value="reelection">Non-Contiguous Re-election Query</option>
        <option value="candidate">Candidate Query</option>
        <option value="electionYear">Election Year Query</option>
        <option value="swingCandidate">Swing Candidate Query</option>
        <option value="party">Party Historical Query</option>
        <option value="pollAccuracy">Poll Accuracy Query</option>
        <option value="swingState">Swing State Query</option>
        <option value="electoralVote">Electoral Vote Query</option>
        <option value="popularVote">Popular Vote Query</option>
    </select></p>
    
    <div id="extradiv" style='display:none;'>
    <span id="el">Candidate Name:</span>
    <input type="text" name="extra" id="extra"/>
    </div>
    
    <p>Submit: 
    <input type="submit" name="submit" value="submit" /></p>

</form>
</body>
</html>