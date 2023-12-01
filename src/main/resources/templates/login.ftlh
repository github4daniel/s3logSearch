<html>
<head>
  <title>Pip Admin Log In</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
	    <link type="text/css" href="css/bootstrap.min.css" rel="stylesheet">
  		<script type="text/javascript" src="js/bootstrap.min.js"></script>
  		<script type="text/javascript" src="js/jquery-1.12.3.min.js"></script>
</head>
    <body>
      <div class="container">
        <h2 align="center">Log in</h2>
        <br>
        <form action="/pipAdminWS/login" method="post">
          <div class="mb-5 row" style="margin: 10px;">
            <div class="col-sm-2 col-form-label">User Name:</div>
            <div class="col-sm-6">
              <input type="text" id="username" name="username" />
            </div>
          </div>
          <div class="mb-5 row"  style="margin: 10px;">
            <div class="col-sm-2 col-form-label">Password:</div>
            <div class="col-sm-6">
              <input type="password" id="password" name="password" />
            </div>
          </div>
          <div class="mb-5 row"  style="margin: 10px;">
            <div class="col-sm-2 col-form-label"></div>
			<div class=" col-sm-6">
              <input type="submit" value="Submit" class="button">
            </div>
          </div>
        </form>
      </div>
    </body>
</html>