
  <h1 align="center">QuickTax</h1>
<p align="center">
  <img width=300 src="https://i.imgur.com/0YN8YoJ.png" />
</p>

## Table of Contents
* [Introduction](#introduction)
* [Features](#features)
* [Technologies](#technologies)
* [Setup](#setup)
* [Team](#team)
* [Contributing](#contributing)
* [Others](#others)

### Introduction
**QuickTax** is a powerful tax management plugin for your server's economy! If you have always 
struggled with managing your server's ever-growing economy, then this may very well just be the 
plugin that you need! Whether it's taxing all players equally or based on their ranks or based 
on their balance, this plugin supports it all! Want to include property tax by taxing based 
on claimblocks? It's possible as well! Even better, schedule tax collections so that you can set 
up once and let the plugin do the rest of the work! Worried about performance? the plugin does 
most of its task asynchronously, minimizing the performance impact that it can have on your server!

The spigot link to download the plugin can be found **[here](https://www.spigotmc.org/resources/quicktax.96495/)**. If you require any assistance, please reach out for support on our **[discord](https://discord.gg/X8VSdZvBQY).** Alternatively, you may also open a github issue.

### Features
<p align="center">
  <img src="https://i.imgur.com/6vzKjyl.gif" />
  <img src="https://i.imgur.com/t8aFkbe.gif" />
</p>

Some of the key features provided by the plugin are as shown below:
- Collect tax from all players
- Collect tax from players by rank
- Collect tax from players by balance
- Collect tax from a specific player
- Collect additional tax depending on the number of claimblocks the player has (requires
  [**GriefPrevention**](https://www.spigotmc.org/resources/griefprevention.1884/))
- Schedule real-time collection of taxes
- Option to play sound to players on collection
- Option for players to view next collection tim
- Option for players to self-pay tax directly to the server
- Track/store player and server tax stats (YAML/MySQL)
- Withdraw money from the server tax balance 
- Setup taxpayer leaderboard via signs and heads!
- PlaceholderAPI support (requires PlaceholderAPI)
- Fully customizable messages (with options for your own language files!)

The features above are just a glimpse of what the plugin is capable of. More detailed guides and 
example setups can be found in our **[wiki](https://github.com/tjtanjin/QuickTax/wiki)**.

### Technologies
Technologies used by QuickTax are as below:
##### Done with:

<p align="center">
  <img height="150" width="150" src="https://brandlogos.net/wp-content/uploads/2013/03/java-eps-vector-logo.png"/>
</p>
<p align="center">
Java
</p>

##### Project Repository
```
https://github.com/tjtanjin/QuickTax
```

### Setup
Setting up the QuickTax project locally would involve the following steps:
1)  First, `cd` to the directory of where you wish to store the project and fork/clone this repository. An example is provided below:
```
$ cd /home/user/exampleuser/projects/
$ git clone https://github.com/tjtanjin/QuickTax.git
```
2) Make any updates/changes you wish to the code. Once ready, you may build the plugin with the following command:
```
mvn clean install
```
If you are satisfied with your work and would like to contribute to the project, feel free to open a pull request! The forking workflow is preferred in this case so if you have the intention to contribute from the get-go, consider forking this repository before you start!

### Team
* [Tan Jin](https://github.com/tjtanjin)

### Contributing
If you have code to contribute to the project, open a pull request from your fork and describe 
clearly the changes and what they are intended to do (enhancement, bug fixes etc). Alternatively,
you may simply raise bugs or suggestions by opening an issue.

Note that as this was my first minecraft plugin, the structure of the codebase leaves more to be
desired. My plan to rewrite the plugin for version 2.0.0 is delayed indefinitely until I am able to
free up more time (or until a volunteer comes along :stuck_out_tongue_closed_eyes:)

### Others
For any questions regarding the project, please reach out for support via **[discord](https://discord.gg/X8VSdZvBQY).**
