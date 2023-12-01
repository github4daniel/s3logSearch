<#macro menubarDiv>

<#switch springMacroRequestContext.requestUri?trim>
  <#case "/pipAdminWS/psconfig">
  	<#assign psconfigActive="class='active'">    
    <#break>
  <#case "/pipAdminWS/psconfigSubmmit">
    <#assign psconfigActive="class='active'">
    <#break>
  <#case "/pipAdminWS/flag_centric">
  	<#assign flag_centricActive="class='active'">    
    <#break>
  <#case "/pipAdminWS/hppqflags">
  	<#assign hppqflagsActive="class='active'">    
    <#break>
  <#case "/pipAdminWS/app_centric">
  	<#assign app_centricActive="class='active'">
    <#break>
  <#case "/pipAdminWS/pipqueryflags">
  	<#assign pipqueryflagsActive="class='active'">
    <#break>
  <#case "/pipAdminWS/pipconfigflags">
  	<#assign pipconfigflagsActive="class='active'">
    <#break>
  <#case "/pipAdminWS/pipconfigOTflags">
  	<#assign pipconfigOTflagsActive="class='active'">
    <#break>
  <#case "/pipAdminWS/pip_retry">
  	<#assign pip_retryActive="class='active'">
    <#break>
  <#case "/pipAdminWS/pip_retry_where">
    <#assign pip_retryActive="class='active'">
    <#break>
   <#case "/pipAdminWS/help">
   	<#assign helpActive="class='active'">
    <#break>
  <#default>
    <#assign psconfigActive="class='active'">
</#switch>





<nav class="navbar navbar-default">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="#">Site: ${sharedVarsActiveEnv}</a>
    </div>
    <ul class="nav navbar-nav">
      <li ${(flag_centricActive!"")}><a href="/pipAdminWS/flag_centric">PIP-QRY Summary</a></li>
      <li ${(hppqflagsActive!"")}><a href="/pipAdminWS/hppqflags">HPPQ flag Summary</a></li>
      <li ${(app_centricActive!"")}><a href="/pipAdminWS/app_centric">PIP-APP Summary</a></li>
      <li ${(pipqueryflagsActive!"")}><a href="/pipAdminWS/pipqueryflags">PIP-QRY Flags</a></li>
      <li ${(pipconfigflagsActive!"")}><a href="/pipAdminWS/pipconfigflags">NON PIP-QRY Config Flags</a></li>
      <li ${(pipconfigOTflagsActive!"")}><a href="/pipAdminWS/pipconfigOTflags">OT Flags</a></li>
	  <li ${(psconfigActive!"")}><a href="/pipAdminWS/psconfig">PSConfig Search</a></li>  
      <li ${(pip_retryActive!"")}><a href="/pipAdminWS/pip_retry">PIP Retry</a></li>
    </ul>
    <ul class="nav navbar-nav navbar-right">
      <li ${(helpActive!"")}><a href="/pipAdminWS/help">Help&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a></li>
    </ul>
    
  </div>
</nav>

</#macro>	