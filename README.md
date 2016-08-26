# eclipse-workspace-launcher
A tiny launcher tool that is able to launch eclipse workspaces. Contributions/Enhancements very welcome.

![Screenshot](/screenshot.png?raw=true "Screenshot")

# How to configure
The tool reads ~/.ecwsrc by default, which contains the configuration of the workspace. This can be changed by passing the "ecws.config" system property to the application.

## Configuration file options

The configuration file is in standard java properties format.

* `screen`: defines the screen number the dialog should appear on (0-N)
* `columns`: how many workspaces should be displayed in a row
* `clean`: should the "clean" checkbox be ticked by default
* `<NAME>_workspace`: path to the workspace for button `<NAME>`
* `<NAME>_eclipse`: path to eclipse executable to use for launching workspace `<NAME>`
* `<NAME>_icon`: path to icon to use for workspace button `<NAME>`

(see also sample.ecwsrc)

# How to use

I personally bound a key to launch this tool. Either select one of the configured Eclipse Workspaces to launch, or press ESC to exit.

In the main window, number keys (1-9) can be used to quickly access (the first 9) workspaces. Buttons are numbered starting from top left.

# How to build

Import into Eclipse. Period :) To run from Eclipse, right-click Launcher.java and select Run As > Java Application. To export a JAR file suitable for key binding etc. right click the project and select Export > Java > Runnable JAR File > Select the "Launcher" launch configuration (you must have launched from the IDE once for it to appear), select a destination and click Finish.
