# Azucraft

## What is this?
In easy words, this plugin keeps track of stuff that happens in a Minecraft world, and writes the data it gathers to a PostgreSQL Database.

## Wait, what? Where is the SQL Schema?
I'll share that one as soon as I consider this plugin stable and all the features I want are complete.
Up until then, study the `SQLOpenHandler` class to figure it out ;)

## How to set this up
* Set-Up a PostgreSQL Database - make sure there are a few-hundred random names in the `mobnames` table.
* Compile a JAR from this source in whatever way you wish.
* Put the JAR into your plugin folder.
* Make sure there is a PostgreSQL JDBC Driver in your classpath when running this on a server.
* Start the server at least once. The plugin will drop a credentials file.
* Edit the credentials file - put the correct PostgreSQL credentials in there.
* Restart the server.
* Have fun trying to figure out what I planned with this ;)

Your Boy,

Feyris-Tan 