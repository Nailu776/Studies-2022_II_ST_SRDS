CREATE KEYSPACE IF NOT EXISTS SpaceBase
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

#USE SpaceBase;

CREATE TABLE SpaceBase.Floors (
  id int,
  airLevelsensor int,
  corridorPopulationSensor int,
  airGenerator boolean,
  PRIMARY KEY (id)
);

CREATE TABLE SpaceBase.AirStorage (
  id UUID PRIMARY KEY,
  airStored counter
);