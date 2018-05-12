<h1>Basically a Levelling Bot, but it gives CANDY and ROLES!</h1>

<br />
<link href="https://fonts.googleapis.com/css?family=Montserrat|Poppins|Roboto" rel="stylesheet">
<br />

The levelling concept of this bot is somewhat Luck-Based. It counts all messages it can see on a Server, and if a certain  number of messages is reached, the user that sent the last message gets a "levelup", and the counter is reset.
That makes the bot quite fun to use, as there is always some Competition on who will get the next levelup.
Also, you can set custom roles per level

<br />
After the Bot has been added to your Server, only Administrators have permission to set it up.
You can give permission to other Users with <code>dango auth [User]</code>, and take permission with <code>dango unauth [User]</code>. 
<br />

<h3>A detailed guide that will be Providing help is about to be made. Stay in touch for updates!</h3>

<h4>Troubleshooting:</h4>
Whenever you have the feeling that a command did not execute correctly, you can click the Bot's Reactions and it will provide information on whats wrong.
<br />
<img src="https://i.imgur.com/uUukSvQ.png"></img>

Command need to start with "@Dango" (Pinging him), "dango" or "dangobot".

<h3>Main Commands:</h3>
<ul>
  <li>dango help [Command] - Displays detailed Information about a command</li>
  <li>dango info - Sends informations about the bot</li>
  <li>dango inviteLink - Sends you the Bot's invite Link</li>
  <li>dango discordLink - Sends you the Invite Link to the Support Discord</li>
  <li>dango donate - Sends you a Donation Link</li>
  <li>dango setup {Variable} {Value} - Lets you set basic preferences for the bot or shows the current setup</li>
</ul>

<h3>Auth System:</h3>
<ul>
  <li>dango auth [User] - Auths an User, requires Permission</li>
  <li>dango unauth [User] - Unauths an User, requires Permission</li>
  <li>dango auths - Shows Authed Users</li>
</ul>

<h3>Informative Commands:</h3>
<ul>
  <li>dango self - Shows what Level you are on</li>
  <li>dango stats - Shows a detailed List of who of the server has how many dangos</li>
  <li>dango records {User} - Shows numbers, on how many messages the user sent within the last days, or how long his average message is.</li>
</ul>

<h3>Dango Setup:</h3>
<ul>
  <li>dango count {Number} - This command either shows the current Rate of Messages that Level ups are Given, or, if a Number is Specified, sets a custom rate.</li>
  <li>dango emoji {Emoji} - This command either shows the current Emoji that represents a "Level", or lets you specify your own Emoji.</li>
  <li>dango action - Shows every currently set up action or lets you specify new Actions. Please use "dango help action" for more detailed information.</li>
  <li>dango give [User] (Number) - Gives the User a specific amount of Levels, requires Permission</li>
  <li>dango take [User] (Number) - Takes a specific amount of levels from the User, requires Permission</li>
  <li>dango revoke - Takes the last given dango from the user who got the last dango</li>
  <li>dango clear - Clears the levels for all users on this Server</li>
</ul>
