# Budget Management Application

###### tags: `Java` `InteliJ` `MySQL`

## 📝 What is the Budget Management Application?
> The following project is a Budget Management Application. This application will allow users to create a monthly budget, manage their expenses, and track their progress.
> Besides these three key features, the application also offers users money saving challenges/games and a feature to remove unwanted subscriptions to save further money. 

## 👩‍🏫 Application Walk Through
### 1️⃣ Budget Management Application Model ###
> Before we began our project, we did some research and designed an application model of how we would want our application to look like once done. As you can see from the screen capture of the model provided below, we decide to make an application with the following 8 distinct sections: login/registration, transactions/records, subscriptions, analysis, budget plan, categories, categories, and challenges/games.
> 
>![](https://github.com/user-attachments/assets/dfcb2c65-09f4-4715-8892-aca617935690)
>![](https://github.com/user-attachments/assets/0b1e5a7a-64a3-407c-b11b-a8de0304ac68)
>
> 
### 2️⃣ Budget Management Application Database ###
> For our project, we used MySQL to store user records. This is how the database looked like at the end.
> 
>![](https://github.com/user-attachments/assets/9fd862b2-9bf4-4cb0-8d83-cdfa92245f1b)
>
> 
### 3️⃣ Budget Management File Structure ###
> We used InteliJ IDEA to make our project but any other IDE would work fine too.
> We organized our project into folders so that it would be easier to find each others work. The folder structure is: auth (for loginapp authorization), connection (to connect to our database), controller (for menu at the bottom of page to navigate user through program), components (gui components for loginapp), and lastly pages (where most of the pages we designed for application are located).
> 
>![](https://github.com/user-attachments/assets/e31eaea0-a95f-404b-bf2a-abc2ca0516c0)
>
> 
### 4️⃣ S.O.M.E Budget Management Application Demo ###
>
#### Section #1 - Login/Registration Page 
>When you first launch the application, you will be prompted to login. If you have a login, great: you can login right away! However, if you're a new user, you can click on the "Create an Account" button which will redirect you to the registration page. Here you will be asked to fill out your full name, username, email, and passcode. If you enter one of the field (for example email field) incorrectly, the page will error asking you to enter correct email address (especially when missing @ sign). Once you create an account and/or if you already have an account you can login! Please note, if you enter your password or username incorrectly the program will error until you input correct credentials. Another feature we added was the eye icon on the side of the password which would allow the user to verify the password they entered before attempting to login.
>
<tr>
    <td><img src="https://github.com/user-attachments/assets/85781922-f059-4dc8-9959-56dc4e0b6943"/></td>
    <td><img src="https://github.com/user-attachments/assets/b73046ad-fd85-4df1-af59-026e18c47c57" width="490" height="578"/></td>
</tr>
<tr>
    <td><img src="https://github.com/user-attachments/assets/137be4fa-6f5c-4131-9064-680311f00c54"/></td>
    <td><img src="https://github.com/user-attachments/assets/4184a196-256b-4acf-ad89-23f36c94ec91"/></td>
</tr>
<tr>
    <td><img src="https://github.com/user-attachments/assets/57e21e82-844f-445f-9610-6911485708b6"/></td>
    <td><img src="https://github.com/user-attachments/assets/4a6890e2-b2ff-4b4c-b249-9ee8aeb3e33a"/></td>
</tr>


#### Section #2 - Transaction History
>When you login to app successfully, you are welcomed by the transaction history page. In this section, users can enter all of their expenses and income they had over the past few months/years. Once this information is entered, users can organize their records by filtering by each expense category to see which expense they spend most on. 
>![](https://github.com/user-attachments/assets/e23c4c4c-a721-4cfd-af2b-c9f1289f5588)
>![](https://github.com/user-attachments/assets/74baff31-ab45-464e-80d2-226d6890c840)

#### Section #3 - Subscriptions
>The next page is the Subscriptions. This section is dedicated only to different subscriptions the user has entered. From here users can enter, edit, and delete any unwanted subscriptions (which will also be automatically updated in the transaction history section).
>![](https://github.com/user-attachments/assets/8cf6ba42-dc06-4719-8b93-d798c1e661ec)

#### Section #4 - Analysis
>After Subscriptions page, we have the Analysis page. In the Analysis section, users will see a visual representation of their expenses and the budget they set. From here, users will see which expenses they spend the most money on and how their spending habits compare to their budget.
>![](https://github.com/user-attachments/assets/a9529a5e-a7bf-4d8a-8611-e5dca39d4fc3)

#### Section #5 - Budget
>The next page is Budget Plan. Here users will be prompted to enter their monthly income (after taxes) and then their budget which would be split between needs, wants, and savings. Once this information is entered, the user can click on the blue calculate budget button at the bottom to see what their budget would look like. If they are confident that this is budget they want, they can click on the green save budget button for it to be saved. Users can then sort by all the budgets they have made from the past.
>![](https://github.com/user-attachments/assets/c43071bc-216e-4794-b2cf-12c94ab1d8ab)
>![](https://github.com/user-attachments/assets/0b0954c5-069c-4f9d-9701-a3d00d8cd2c7)

#### Section #6 - Accounts Information
>After Budget Page, we have Accounts page. Here users will be asked to enter all of their financial accounts. Financial accounts will then have their amounts populated through the transaction history by the user. If the user chooses to edit their financial accounts page or delete it, they can do so from the accounts information page. For deletion of page to occur however, user will need to have no money in their account, otherwise if they do have money, account will error and not delete. 
>![](https://github.com/user-attachments/assets/6e293e89-2db3-4161-80a9-f9ce9b3765e2)
>![](https://github.com/user-attachments/assets/4bca6270-45b5-4e30-a481-31f0128b1876)
>![](https://github.com/user-attachments/assets/63991f64-ad33-416b-8d4d-56fb53eb5090)

#### Section #7 - Categories Information
>The next page is Categories pafe. Here users will be able to view and enter any further expenses they may have. Whether that be donations, hobbies, or petcare this section allows user to enter a new expense category which they would be able to use later in transaction page. If you hover over the image in the categories information page, a description of the category expense should appear. 
>![](https://github.com/user-attachments/assets/37f4adb9-676d-4d6b-ba10-035f4ac78be4)
>![](https://github.com/user-attachments/assets/0569455c-eb29-4f2c-9851-6b2a4642df53)
>![](https://github.com/user-attachments/assets/533685bb-dbbf-466c-b359-add1278a811d)

#### Section #8 - Challenges/Badges
>Laslty, we have the Challenges page. In this section, users will have a chance to play a mini game that promotes healthy financial milestones, in particular about spending less money. The current challenge we have is the "No Spend Day" which as name suggests, asks user not to spend money for one day (or more). Once the user clicks to start challenge, they will need to enter records with at least a 1 day gap in the transaction page. Once this is done, users can go back to the page to claime their badge which will appear in the form of a pop up (with music). 
>![](https://github.com/user-attachments/assets/f59dc32b-cdd6-466b-acf8-0cc3a28de99c)
>![](https://github.com/user-attachments/assets/2e2fba1a-f4aa-48d3-85fe-21e18727d50d)
>![](https://github.com/user-attachments/assets/582049a3-2769-4a92-9b64-9592ae28bd79)
>![](https://github.com/user-attachments/assets/aa321d92-b801-468e-b849-c417e70783a6)
>![](https://github.com/user-attachments/assets/4e907882-9950-4c14-add5-8b9221c7b043)

## 😃 Thank you for viewing our application ##
> I hope you enjoyed the little walk through of the Budget Management Application. If you have any questions and/or concerns let me know! Don't forget to leave a star⭐️.
