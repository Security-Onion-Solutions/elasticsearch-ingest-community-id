# Elasticsearch community-id Ingest Processor

Explain the use case of this processor in a TLDR fashion.

## Usage


```
PUT _ingest/pipeline/community-id-pipeline
{
  "description": "A pipeline to ingest community_id",
  "processors": [
    {
      "community_id" : {
        "field" : ["source_ip", "source_port", "destination.ip", "destination.port", "transport"],
        "target_field" : "community_id"
      }
    }
  ]
}

PUT /my-index/my-type/1?pipeline=community-id-pipeline
{
  "source_ip" : "192.168.1.52",
  "source_port" : "54585",
  "destination": {
    "ip": "8.8.8.8",
    "port": "53"
  },
  "transport": "UDP"
}

GET /my-index/my-type/1
{
  "source_ip" : "192.168.1.52",
  "source_port" : "54585",
  "destination": {
    "ip": "8.8.8.8",
    "port": "53"
  },
  "transport": "UDP"
  "community_id": "1:d/FP5EW3wiY1vCndhwleRRKHowQ="
}
```

## Configuration

| Parameter | Required | Use |
| --- | --- | --- |
|  `field`   | Yes | Array of fields in the following order `[source_ip, source_port, destination_ip, destination_port, transport_protocol]`, in case the field is nested use a dot operator|
| `target_field`  | Yes | name of the field where community_id needs to be injected |

## Setup

In order to install this plugin, you need to create a zip distribution first by running

```bash
gradle clean check
```

This will produce a zip file in `build/distributions`.

After building the zip file, you can install it like this

```bash
bin/elasticsearch-plugin install file:///path/to/ingest-community-id/build/distribution/ingest-community-id-0.0.1-SNAPSHOT.zip
```

## Bugs & TODO

* There are always bugs
* and todos...

