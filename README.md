# RapBot
Raspberry Pi Robot for autonomous land exploration.

## Introduction
This software guidebook provides an overview of the RapBot system. It includes a summary of the following:

1. The requirements, constraints and principles behind the project.
2. The software architecture, including the high-level technology choices and structure of the software.
3. The infrastructure architecture and how the software is deployed.
4. Operational and support aspects of the website.

## Context
RapBot is a distributed system used to control an autonmous land vehicle to explore indoor spaces autonomously. The system runs on a [Raspberry Pi 2 Model B](https://www.raspberrypi.org/products/raspberry-pi-2-model-b/). Using an [Adafruit Motor Hat](https://www.adafruit.com/product/2348) -- with stepper motors attached to a [robot kit](https://www.adafruit.com/product/2939) -- the RapBot will create a two-dimensional map of its surroundings. Users can then view a heat map of the space, where temperature is determined by the rate of change between runs of a specific point.

The purpose of RapBot is to provide a provide a proof-of-concept for indoor-space using autonmous vehicles.

__Placeholder for context diagram__

### Users
RapBot has a single type of user. Users have acess to start and stop the autonomous exploration, delete stored maps, and view a heat map. All functionality is provided to users through a web interface run on the Raspberry Pi when RapBot starts.

### External Systems
RapBot currently has no external dependencies.

## Functional Overview
This section provides a summary of the functionality provided by the RapBot system.

### Exploration
RapBot's primary purpose is autonomous exploration. For this purpose RapBot must scan its environment to detemrine its location within the space it is exploring. RapBot stores sensor data together with the two-dimensional map, so that we can test algorithmic changes on historic data.

### Two-Dimensional Maps
RapBot is used to create two-dimensional maps of the explored space. Each time RapBot runs, it creates a new map. All maps are stored locally on disk. Maps contains metadata about the time the map was created. Each map encodes information about how points in a space are reachable from nearby points. One way to think about the maps, is to think of super0imposing a grid over a floor plan of an apartment. RapBot explores the apartment -- not knowing where in the grid it began -- and tracks which points on the grid it can access from its current point. This information lends itself well to a sparse graph format. Where each point on the grid is a node and there is a vertex between its neighboring points that RapBot can access.

### Heat Map
The heat map is a visualization of RapBot's exploration over time. RapBot aligns each of the two0dimensional maps stored on its filesystem and transforms change over time to the temperature range. Temperature is calculated by the relationship between the number of transitions between the node being accessible, and not, and the number of maps processed.

### Users
A user is any person with access to the web interfaces running on RapBot. On start, RapBot will launch an interface to view heat maps, historic maps, and the current map.

## Quality Attributes
This section provides information about the desired quality attributes (non-functional requirements) of the RapBot system.

### Performance
Since RapBot's purpose is exploration, it can be difficult to define a direct measure of performance. To ensure that performance does not degrade significantly, we measure RapBot's performance on three dimensions: duration of exploration, percentage of accesible space explored, delay between detecting an impass and altering course. Because RapBot stores sensor data for each run, we can run simulations on code changes to meausre their impact along these dimensions.

### Scalability
We expect RapBot's filesystem to have sufficient space for multiple runs. As run length, the number of sensors, and the number of runs,  increases we will need alternative ways to store this data. Also, we expect RapBot's memory usage to scale with the size fo the space to explore and the number of sensors feeding it data. Due to constraints in the initial runtime environment RapBot must initially operate with the following constrainst:

* Memory: less than 256 MB
* CPU: less than 50% CPU utilization
* Disk: less than 1 GB per run.

### Security
RapBot is not written with security in mind. All web interfaces are accesible to anyone with access to the host machine. Users are not authenticated in any way. Data is not encrypted on disk. 

### Availability
Because of RapBot's educational nature, there are not strict availibility targets.

### Internationalisation
All user interface text will be presented in English only, with the possibility of providing Spanish translations.

### Localization
All information will be formatted for the American English locale only. All time stamps will be stored as milliseconds since UNIX epoch time.

### Browser compatibility
RapBot's web interfaces should work consistently on the following browsers:
* Chrome
* FireFox

## Contraints
This section provides information about the constraints imposed on the development of the RapBot system.

### Budget
RapBot does not have an acutal budget, so all code must run natively on the host machine and all data must be stored on the local filesystem. When applicable, free cloud-based alternatives may be used. Ideally, RapBot must run on a single Raspberry Pi, however, because of the constraints in the number of sensors a single Rapsberry Pi can house additional hosts may be used.

### Time
RapBot is maintained by a single developer, so solutions will typically be incremental in nature. When possible, existing solutions will be used to speed development.

## Principles
This section provides information about the principles adopted for the development of the RapBot system.

### Package by component
To provide a simple mapping of the software architecture into the code, the package structure of the code reflects a “package by component” convention rather than “package by layer”.

__Placeholder for packaging diagram__
