#!/bin/bash

TIMESTAMP=$(date +%F_%H-%M-%S)
pg_dump -h host.docker.internal -U postgres -d novaBackend > /backups/backup_$TIMESTAMP.sql

echo "Backup hecho: backup_$TIMESTAMP.sql"
