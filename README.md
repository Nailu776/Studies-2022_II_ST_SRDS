# SRDS-2022S9

Repository for a project on SRDS

# Authors
Julian Helwig - https://julian.helwig.tech/#/ https://github.com/Nailu776

Seweryn Kopeć - https://github.com/SewerynKopec

# Description

The projects implements a simulation of an air distributing sequence on a space station.

Monitor tracks the air depletion on seperate floors and assigns units of air to the correct segments.

Each floor has its thread and the information about air is stored in Cassandra database that works on multiple clusters.

The simulation shows how different consistency settings of Cassandra  affect the readings on the monitor.
