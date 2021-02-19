# SimpleSchematic
This is a simple way to save structures and paste structures in your spigot minecraft world

The reason this is being made is due to the over complexity of using schematics without worldedit
or one of its forks, as well as the added complexity that version 1.13 and above brought to the table
by getting rid of the material id system and instead moving towards namespace id's. The goal of this project is
to simplify the entire process and make it fast and reasonable.

The first few goals of the project are: 
1.) Allow the saving of structures to a new .schem file
2.) Allow the reading of structures from a new .schem file
3.) Have the ability to paste structures
4.) Ensure that the NMSAbstraction interface covers enough so that it can allow more advanced users to create their own pasting method
5.) Ensure that the .schem file is as small and compact as possible while maintaining fast read/write
6.) Making sure that pasting even without NMS is fast and fluid
