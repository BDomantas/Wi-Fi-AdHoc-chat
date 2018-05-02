# Wi-Fi-AdHoc-chat

Serverless chat application where each of the computers acts as a server and a client at the same time.
Messages are encrypted with RSA and routed manually.

E.g. If computer A sends message to C, message gets manually re-routed by node B (client of B is not aware of that)

A---B---C

## Set up

All of the computers has to be sharing a wifi hotspot. One of the ways to do this is like this:
First a manual connection has to be made through a control pannel
Then following commands executed in a terminal
```
netsh wlan set profileparameter "Hotspot" connectiontype=ibss connectionmode=manual
netsh wlan connect ""Hotspot
netsh interface ipv4 set address "Wi-Fi" static 192.168.5.1 255.255.255.0
```
