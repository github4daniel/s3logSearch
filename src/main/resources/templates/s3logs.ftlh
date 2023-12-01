<html>
  <#import "/spring.ftl" as spring />
  <#import "/macros/header.ftlh" as hdr>
    <#import "/macros/menubar.ftlh" as mnu>
      <@hdr.headerDiv pageTitle="S3 Log Search" />
      <body>
        <style>
          .accordion {
            width: 70%;
            display: flex;
            flex-direction: column;
            margin: 10px;
          }

          .accordion-section {
            background-color: #f9f9f9;
            border: 1px solid #ccc;
            margin-bottom: 10px;
            overflow: hidden;
          }

          .accordion-header {
            padding: 10px;
            cursor: pointer;
            user-select: none;
            transition: background-color 0.3s;
          }

          .accordion-content {
            padding: 10px;
            display: none;
          }

          .active {
            background-color: #e0e0e0;
          }

          .active .accordion-content {
            display: block;
          }

          .button {
            padding: 6px 12px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            margin-left: 10px;
            cursor: pointer;
          }
        </style>
        <@mnu.menubarDiv />
        
        <div class="container">
          <h2 align="center">S3 Log Search</h2>
          <br>
          <form action="/pipAdminWS/s3logs" method="post">
            <div class="mb-3 row">
              <div class="col-sm-2 col-form-label"> Select a Domain Type: </div>
              <div class="col-sm-6">
                <@spring.formMultiSelect "criteria.entryType" , entries, 'style="margin-left:10px"' />
              </div>
            </div>
            <div class="mb-1 row">
              <div class="col-sm-2"></div>
              <div class="col-sm-6" style="margin-left:6px;">
               <@spring.showErrors "<br>"/>
              </div>
            </div>
            <div class="mb-3 row">
              <div class="col-sm-2 col-form-label"> Search Text: </div>
              <div class="col-sm-10">
                <@spring.formInput "criteria.searchText1" , 'style="margin:10px", size="100"'/> 
                              &&
                <@spring.formInput "criteria.searchText2" , 'style="margin:10px", size="100"' />
               				  &&
                <@spring.formInput "criteria.searchText3" , 'style="margin:10px", size="100"' />
              </div>
            </div>
            <@spring.bind "criteria.searchText1"/>
            <div class="mb-1 row">
              <div class="col-sm-2"></div>
              <div class="col-sm-6" style="margin-left:6px;">
               <@spring.showErrors "<br>"/>
              </div>
            </div>
            <div class="mb-3 row">
              <div class="col-sm-2 col-form-label"> Select Time Frame: </div>
              <div class="col-sm-10">
                <div class="accordion">
                  <div class="accordion-section">
                    <div class="accordion-header">Date/Time Range</div>
                    <div class="accordion-content">
                      <label for="startDateTime" class="date-time-input-label">Start Date/Time:</label>
                      <input type="datetime-local" id="startDateTime" name="startDateTime" step="1" value="${criteria.startDateTime}" class="time-input">
                      <label for="endDateTime" class="date-time-input-label">End Date/Time:</label>
                      <input type="datetime-local" id="endDateTime" name="endDateTime" step="1" value="${criteria.endDateTime}" class="time-input">
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="mb-3 row">
              <div class="col-sm-2 col-form-label">Limited Records:</div>
              <div class="col-sm-6">
                <@spring.formInput "criteria.numOfRec" , 'style="margin:10px"' />
                <input type="submit" value="Search" class="button">
              </div>
            </div>
            <#if searchResultsList?has_content>
              <br>
              <hr align="center" />
              <br>
              <div class="mb-3 row">
                <div class="col-sm-2 col-form-label">Save Results in File: </div>
                <div class="col-sm-6">
                  <@spring.formInput "criteria.fileName" 'size="100"'/>
                </div>
                <div class="col-sm-4">
                  <input type="submit" name="download" value="Download" class="button">
                </div>
              </div>
              <h5>${recordsOfTotal} records displayed, you can download the file to view all search records.</h5>
              <table class="table">
                <thead>
                  <tr>
                    <th scope="col" width="100">domain</th>
                    <th scope="col">key</th>
                    <th scope="col">line</th>
                  </tr>
                </thead>
                <tbody>
                  <#list searchResultsList as searchResult>
                    <tr>
                      <td>${(searchResult.domainDisplay!"")}</td>
                      <td>${(searchResult.s3ObjectSummary.key!"")}</td>
                      <td>${(searchResult.line!"")}</td>
                    </tr>
                  </#list>
                </tbody>
              </table>
              <#elseif searchResultsList??>
                <h5>No records have been found</h5>
                <#else>
            </#if>
          </form>
        </div>
        <script>
          const accordionSections = document.querySelectorAll('.accordion-section');
          accordionSections.forEach(section => {
            const header = section.querySelector('.accordion-header');
            const inputs = section.querySelectorAll('.time-input');
            header.addEventListener('click', () => {
              accordionSections.forEach(otherSection => {
                if (otherSection !== section) {
                  otherSection.classList.remove('active');
                  const otherInputs = otherSection.querySelectorAll('.time-input');
                  otherInputs.forEach(input => {
                    input.checked = false;
                    input.value = ''; 
                  });
                }
              });
              section.classList.toggle('active');
            });
          });
          
        </script>
      </body>
</html>