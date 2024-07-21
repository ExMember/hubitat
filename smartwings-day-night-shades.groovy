metadata {
   definition (name: "My Z-Wave Switch", namespace: "MyNamespace", author: "My Name") {
      capability "Actuator"
      capability "Switch"
   }

   preferences {
      // none for now -- but for Z-Wave devices, this would often
      // include preferences to set configuration parameters in addition
      // to conventional Hubitat logging preferences, etc.
   }
}

def installed() {
   log.debug "installed()"
}

def updated() {
   log.debug "updated()"
}

def parse(String description) {
   // This is where incoming data from the device will be sent.
   // For now, just log the raw data (we will discuss ways to handle
   //  this later):
   log.debug "parse: $description"
}

def on() {
   // TO DO: Required command from Switch capability, logging for now:
   log.warn "on() not yet implemented"
}

def off() {
   // TO DO: Required command from Switch capability, logging for now:
   log.warn "off() not yet implemented"
}
