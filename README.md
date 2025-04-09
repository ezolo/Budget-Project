# Budget Management Application

###### tags: `Java` `Eclipse` `VS 4.34`

## 📝 What is the Budget Management Application?
> The following project is a Budget Management Application. This application will allow users to create a monthly budget, manage their expenses, and track their progress.
> Besides these three key features, the application also offers users money saving challenges/games and a feature to remove unwanted subscriptions to save further money. 

## 👩‍🏫 Application Walk Through
> 🚧Project in Development🚧

## 🛠️ Instructions For Team 
>1.) Bitbucket<br>
>&nbsp;&nbsp;&nbsp; 1.1) Accept Inivitation to work on project<br>
>&nbsp;&nbsp;&nbsp; 1.2.) Change branch to "Main", write your own name for branch and create your own branch<br>
>2.) Setting up GitHub Desktop <br>
>&nbsp;&nbsp;&nbsp; 2.1.) Install GitHub Desktop: Download and install GitHub Desktop from the official GitHub website. https://desktop.github.com/download/<br>
>&nbsp;&nbsp;&nbsp; 2.2.) Sign in: Sign in to GitHub Desktop with your GitHub account. <br>
>&nbsp;&nbsp;&nbsp; 2.3.) Clone a Repository:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2.3a.) Navigate to the GitHub repository you want to work with. https://github.com/ezolo/Budget-Project<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2.3b.) Click "Code" and then "Open with GitHub Desktop".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2.3c.) Choose a local directory to clone the repository to.<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 2.3d.) Click "Clone repository".<br>
> 3.)  Importing the Repository into Eclipse <br>
>&nbsp;&nbsp;&nbsp; 3.1.) Open Eclipse: Launch Eclipse IDE.<br>
>&nbsp;&nbsp;&nbsp; 3.2.) Import Projects:<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2a.) Go to "File" > "Import".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2b.) Expand "Git" and select "Projects from Git".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2c.) Click "Next".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2d.) Choose "Existing local repository".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2e.) Follow the prompts to provide the necessary repository information (e.g., URI, local destination, branch).<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 3.2f.) Select Projects: Select the desired project(s) and click "Finish".<br>
>4.) Working with Eclipse and GitHub Desktop <br>
>&nbsp;&nbsp;&nbsp; 4.1) Make Sure to grab JDBC Connector Jar for MySQL found here: https://dev.mysql.com/downloads/connector/j/<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  4.1a.) Click "Platform Independent" and download zip. To add jar to project, follow steps from class JDBC slides<br>
>&nbsp;&nbsp;&nbsp; 4.2.) When you make some changes, make sure are on your branch that you created (should be title of project). If not switch to your branch. Do not push changes to "Main", we will be doing that together to avoid merge conflict. <br>
>&nbsp;&nbsp;&nbsp; 4.3.) Once you make some changes in your branch, you will commit changes<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.3a.) In Eclipse, right-click the project (where you made changes) and select "Team" > "Commit".<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.3b.) Add a commit message (what changes you made) and click "Commit".<br>
>&nbsp;&nbsp;&nbsp; 4.4.) Once you commit changes to your branch, you will need to push your changes to repo.<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.4a.) In GitHub Desktop, click "Push" to push the changes to the remote repository.<br>
>>&nbsp;&nbsp;&nbsp; 4.5.) Before making any new changes, it's recommended for you to Pull new changes from repo<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.5a.) In GitHub Desktop, click "Fetch" to get the latest changes from the remote repository.<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.5b.) Click "Pull" to merge the changes into your local repository<br>
>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 4.5c.) After pulling changes from GitHub Desktop, refresh your Eclipse project to see the updates. <br>
>5.) Setting up MySQL on your end<br>
> &nbsp;&nbsp;&nbsp;2.1) Download MySQL Workbench from: https://dev.mysql.com/downloads/workbench/<br>
> &nbsp;&nbsp;&nbsp;2.2) When download finished, enter MySQL Workbench and if there is no connections available, create new "local host" connection. There are many tutorials online how to do this, you can use this youtube video as example: https://www.youtube.com/watch?v=QOQ2XV2n-pk<br>
> &nbsp;&nbsp;&nbsp;2.3.) Once new connection created, open new query tabe: File --> New Query Tab and run the DDL and DML files. DDL creates schema and tables, DML creates mock data<br>
> &nbsp;&nbsp;&nbsp;2.4.) Once you run queries, refresh budget_management database: right click on budget_management schema and select "refresh all". <br>
> &nbsp;&nbsp;&nbsp;2.5) Confirm that 6 tables have been created<br>
> &nbsp;&nbsp;&nbsp;2.6) Confirm that there is mock data in users table by running: select * from users; There should be 3 records<br>

> **Side Notes**<br>
> Remember to Pull new changes before making any new changes of your own.
> We will work together to merge changes to main branch and solve any merge conflicts together.

## 😃 Thank you for viewing our application ##
> I hope you enjoyed the little walk through of the Budget Management Application. If you have any questions and/or concerns let me know! Don't forget to leave a star⭐️.
