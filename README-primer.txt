SPECVIEW
============

SpecView is a program that provides a wide range of spectral
analysis tools to the user. It allows the display and the manipulation
of spectra.

NECESSITY
=========

Xamine allows the display and viewing of spectra. It however does not
allow the manipulation and writing of spectra to files, the fitting of
curves to displayed spectra, etc.

The original spec allows the display and manipulation of spectra.
However, it only runs on VMS systems, not on the more popular UNIX and
windows machines of today.

Program Spec was written using JAVA, giving it increased platform
independence. It can run on any machine with a JAVA Virtual Machine
installed. 

SYSTEM REQUIREMENTS:
====================

--> JAVA Virtual Machine Version 1.2
--> Windows 95, 98, NT, Solaris (currently)

--> Within a couple of months, it will be supported on Digital UNIX
and many other operating systems.
This is because a new version of the JAVA development kit was
used. This version (1.2) has only just been released for Windows
machines and Solaris.

THE SPECVIEW WINDOW
===================

The Menu Bar
============
Below the title bar is SpecView's menu bar. The menu's currently
defined are
File
  : Controls the opening of spectra from files, and allows you exit
	from SpecView.
  :: View chapter X on "Spectrum Manipulation" for details
  --> Open
  --> New Graph
  --> Exit
View
  : Allows the display of windows for interaction with spectra
  :: View chapter X on "Spectrum Manipulation" for details
  : allows you change the look and feel of SpecView (for platforms
	that support multiple looks and feels
  --> Spectrum Manipulator
  --> Change Look and Feel
        --> Windows
        --> Motif
        --> Cross Platform
Window
  : Allows you cascade, tile or minimize the windows displaying graphs
	in SpecView.
  :: View Chapter X on "Graphs" for details
  --> Cascade
  --> Tile
  --> Minimize
Graph
  : Duplicates the tasks that can be performed on a window displaying
	a graph
  :: View Chapter X on "Graphs and Display" for details
  --> Display Spectrum
  --> Print Graph
  --> Enable screen capture of Graph
  --> Overlay Spectrum
  --> Refresh
  --> Redraw plot
  --> Clear graph
  --> Close Window
Help
  : get information on SpecView, and some on-line help
  --> Help Contents
  --> About Spec

The Standard Tool Bar
=====================
Below that is a collection of button icons, which duplicate some tasks
on the menu bar. The icons might be unfamiliar at first. However,
moving your mouse over each icon and leaving it there for some time
shows some information on the utility of the icon. The icons are also
duplicated on the menu bar, so with time, you should be able to tell
their functions.

The toolBar can be dragged out of the main window. By dragging on the
extreme left also, it can be made vertical, and placed to the left of
the main plotting area.

The Plotting Area
=================
This takes most of the space of the SpecView main window. In here,
spectra are displayed and viewed in mini-window. The plotting area
also handles the window that are displayed.

The maximum number of windows that can be open at any time is fixed at
24. The plotting area will not allow you open more than that. 

The Status Bar
==============
Gives some information when tasks are being performed so you can keep
track with what is going on behind the scenes.

Accelerators
============
An acceleration is a sequence of Keyboard strokes which act as a
short-cut to a menu selection. The accelerators are always placed
to the right of the menu items when the menu is pulled down. These
sequence of key strokes can just be typed and they perform the same
functions as selecting form the menu.

The accelerators are defined below
==================================
Accelerator	Meaning
-----------	-------
Ctrl N		Open Empty Spectrum Window
Ctrl O		Open and Display from a Spectrum File
Ctrl M		Show dialog for manipulating spectra
Ctrl T		Show Transcript Window

Ctrl Q		Exit Spec

Ctrl S		Display Spectrum in selected graph
Ctrl P		Print Graph
Ctrl L		Overlay Spectrum in selected graph
Ctrl F		Refresh selected graph
Ctrl D		Redraw plot in selected graph
Ctrl W		Close selected window

SPECTRUM MANIPULATION
=====================

Spectra can exist in three formats. The original smaug format that has
been traditionally used. (*.spc)
An extended format based on SPCLIB but which also stored the
uncertainty values in the file. (*.spx)
An ascii file of the channels and their spectrum values, stored in
textual form as rows and columnar data. This can be read with any text
editor or viewer. (*.spd)

Currently, only the original spc (*.spc) format is supported on spec. 
The other formats will be supported in a future release.

Spectra are read from files in a variety of ways. One way is through
the file pull down menu.
File --> Open and Display Spectrum
Select a file from the file dialog box and click on the button
labeled "open". This reads the spectrum and displays it in a new
graph window.

A more robust method exists, which separates reading a spectrum from a
file from the actual display of the spectrum. This can be accessed in
three ways:
1) Pull down menu 
	View --> Spectrum Manipulator
2) Toolbar icon

This produces a dialog box with a list box on the left and a set of
buttons on the right. The list box holds a list of all the currently
opened spectra. On the right are a set of buttons:
	Read --> Open Spectrum
	Write --> Write Spectrum
	Close --> Close Spectrum
	Display --> Display Spectrum
	Edit --> Modify/View Spectrum
	Information --> Spectrum and Graph Information

1) Open Spectrum
This produces a file chooser and allows the user select a spectrum
file to read. The read spectra is then read in and added to the list
box.

2) Write Spectrum
This takes the spectrum selected in the list box and allows you write
it to a specific file in a specific spectrum format.
--> Select a spectrum
--> Clicking on the button brings a window in which you select a
spectrum format.
--> Then click on the "Yes" button, which brings up a dialog for you to
type a filename.
This writes the spectrum to a file.

3) Close Spectrum
This closes a spectrum that has been read and closes all graphs that
displayed it (as the primary spectrum).
--> Select a spectrum
--> Click on the button to close that spectrum and all graphs of it

4) Display Spectrum
This displays the selected spectrum in a graph window. If the spectrum
is already being displayed in a frame, it just brings that frame to
the front.
--> Select a spectrum
--> Click on the button to display spectrum

5) Modify/View Spectrum
This brings up a dialog box in which you can edit some of the spectrum
attributes, and view some extra information also.
--> Select a spectrum
--> Click on the button to bring up the dialog box
    :: The name of the spectrum can be changed. Type a new name in the
box.
    :: The date at which the spectrum file was last modified can be
changed also. Select either "current date" or "original date"
    :: Information regarding the spectrum like the number of channels,
the maximum count and the channels with the maximum counts can also be
viewed.
    :: Click YES to set the changes or cancel to cancel without
changing the spectrum attributes.

6) Spectrum and Graph Information
This allows you view other information specific to the display of the
spectrum, like the file that the spectrum was opened from, etc.
--> Select a spectrum
--> Click on the button to bring up the dialog box
--> Click CLOSE to close this dialog


MANIPULATION OF GRAPH WINDOWS
=============================
Operations on all the graph windows can also be performed to assist in
the viewing and display of the graphs.
All the windows can be:
1) minimized
2) restored
3) tiled
4) cascaded

These operations can be performed from in two ways:
1) Pull down menu:
Window
  --> Tile
  --> Cascade
  --> Minimize
  --> Restore
2) Icons on the standard tool bar


GRAPH DISPLAY
=============

Graph Window
============
Opening and Closing graph windows
---------------------------------
Open a new Graph window from the main window menu bar
File
  --> New Spectrum Graph
Window
  --> New Spectrum Graph
Standard Tool bar

This opens up a new spectrum window. A graph can then be displayed in
it.

Close a graph window by clicking on the close button on its title bar,
or selecting close from its menu. A graph window can also be closed
from the standard menu bar (under Graph) and tool bar by clicking the
close graph window icon on the standard tool bar.

If a graph is being displayed in  the graph window, you will be
prompted for confirmation to close the window first.

************************************************************

Each feature in the Graph Window can be accessed through three UI's,
1) the Pull down menu on the Graph window
  Graph
    --> ...
2) Pull down menu on main SpecView window
  Graph
    --> ...
3) Graph Tool bar

Display Spectrum
----------------
Spectra can be displayed in the graph windows and the visual
representation of the spectra can be manipulated from there. 

A spectrum can be displayed in 3 ways, two of which have already been
described:
1) From Pull down menu
File
  --> Open and Display Spectrum
2) From Spectrum Manipulator

The third way is by opening an new graph window and displaying a
spectrum in it.
This can be performed through the 3 UI's described above:
Menu selection: Display Spectrum

All these create a dialog box with the list of the opened
spectra. Select a spectrum and hit OK to display it in the graph window.

Printing Graph
--------------
Printing can be accomplished in 2 different modes.
1) Normal Postscript printing
2) Screen capture printing

The normal postscript printing has some problems that are being worked
like the quality of the printout, inconsistent abnormalities when
printing: e.g, sometimes, the display is messed up after printing. 

This can be accessed from the 3 UI's described above
Menu selection: Print

The option is to use the on-screen printing. SpecView supports this by
temporarily putting the graph into another stand-alone window which
can be captured and printed out.

This can be accessed from the 3 UI's described above
Menu selection: Enable on-screen printing

Clearing graph
--------------
A graph window can be cleared of the spectrum displayed in it. 
Select clear from the UI's.

Refresh graph
-------------
This just refreshes the display. Use this if it seems like something
is wrong with the display.

Redraw graph
------------
This redraws the spectrum from the spectrum arrays. This can be
time-intensive, especially for 2 dimensional spectra.


Graph Manipulation
==================
The graph can be manipulated in many ways. Each graph typically has:
--> a title,
--> a key and
--> plotting area.
The plotting area has
--> some axes and
--> the main graphing area.
All of these are manipulated separately.

Title
-----
This is the title of the graph. Double click on it to change its
attributes, including its text string and color.

Key
---
Cannot be manipulated by user

Plotting Area
-------------
Axes
----
Any axis can be manipulated by double-clicking on it. You can then
change the range of the axis, the color with which it is drawn, the
title of the axis, etc.

Main Graphing area
------------------
Zooming is supported on the graphing area. Using the normal (left)
mouse button. Click on the graphing area and drag to define a zooming
area. A rectangle will be drawn to represent the zooming area
required. Release the mouse. The graph will be zoomed to the bounds
zoomed. 
** Note that zooming can also be achieved by editing the axis and
changing the range. This is more precise than direct zooming, since
you can define the bounds you require precisely.

The graph of the spectrum can also be manipulated extensively at the
graphing area using the pop-up trigger. Click on the mouse pop-up
trigger of your platform ...
e.g Windows --> Right mouse button
    MAC --> Ctrl + Mouse
    Motif --> Right mouse button
This brings up the pop-up menu described below

DataScale
--> Linear
--> Log 10
--> Log E
Reset Axes Range
Refresh
Redraw
Show / Hide Uncertainty
Show / Hide GridLines

Data Scale
----------
Switch between Linear and Log scales. Two Log scales are supported,
natural Log (Log E) and Log to base 10 (Log 10).

Reset Axes Range
----------------
You can reset the axes range after manipulating the axes either
by zooming or direct axis editing. This automatically zooms to
accommodate the whole graph without clipping any part out.

Refresh
-------
Just refreshes the display of the graphing area.

Redraw
------
Redraws the spectra displayed into the graphing area. Might be
time-consuming, especially for big 2 dimensional spectra.

Show / Hide Uncertainty
-----------------------
Toggles between showing and hiding the uncertainty, especially for 1-d
spectra. No method of visualizing the uncertainty for 2-d spectra has
been developed yet.

Show / Hide GridLines
-----------------------
Toggles between showing and hiding gridlines.

