import groovy.transform.Field

@Field static final Map commandClassVersions = [
  0x20: 2, // Basic
  0x22: 1, // Application Status
  0x26: 3, // Switch Multilevel (device supports v4, hub does not)
  0x50: 1, // (Basic) Window Covering
  0x55: 1, // Transport Service (device supports v2, hub does not)
  0x59: 1, // Association Grp Info
  0x5A: 1, // Device Reset Locally
  0x5E: 2, // Zwaveplus Info
  0x60: 4, // Multichannel
  0x6C: 1, // Supervision
  0x70: 4, // Configuration
  0x72: 2, // Manufacturer Specific
  0x73: 1, // Powerlevel
  0x7A: 5, // Firmware Update Md
  0x80: 1, // Battery V1
  0x85: 2, // Association
  0x86: 3, // Version
  0x87: 3, // Indicator
  0x8E: 3, // Multi Channel Association
]

metadata {
   definition (name: "SmartWings Day/Night Cellular Shades", namespace: "ExMember", author: "Damien Burke") {
      capability "Actuator"
      capability "WindowShade"
   }

   preferences {
      // Z-Wave devices will often include preferences for configuration parameters here
      input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
      input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
   }
}

def installed() {
   log.debug "installed()"
   // Sometimes you may wish to initialize attributes to default values here or
   // call refresh() to fetch them (not implemented in this driver currently, but
   // a reasonable command to implement for many devices)
}

def updated() {
   log.debug "updated()"
   log.warn "debug logging is: ${logEnable == true}"
   log.warn "description logging is: ${txtEnable == true}"
   if (logEnable) runIn(1800, "logsOff")  // 1800 seconds = 30 minutes
   // In drivers that offer preferences for configuration parameters, you might also iterate over
   // then and send ConfigurationSet commands as needed here (or in configure() if implemented)
}

// handler method for scheduled job to disable debug logging:
void logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value:"false", type:"bool"])
}

def parse(String description) {
   if (logEnable) log.debug "parse description: $description"
   def cmd = zwave.parse(description, commandClassVersions)
   if (cmd) {
      zwaveEvent(cmd)
   }
}

/*
 **********************
 * zwaveEvent Methods *
 **********************
 */

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
   if (enableDebug) log.debug "BasicReport:  ${cmd}"
}

def zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd) {
   if (logEnable) log.debug "SupervisionGet - SessionID: ${cmd.sessionID}, CC: ${cmd.commandClassIdentifier}, Command: ${cmd.commandIdentifier}"
   hubitat.zwave.Command encapCmd = cmd.encapsulatedCommand(commandClassVersions)
   if (encapCmd) {
      zwaveEvent(encapCmd)
   }
   sendHubCommand(new hubitat.device.HubAction(zwaveSecureEncap(zwave.supervisionV1.supervisionReport(sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0).format()), hubitat.device.Protocol.ZWAVE))
}

def zwaveEvent(hubitat.zwave.Command cmd) {
   // Just noting that the data was parsed into something we aren't handling in this driver:
   if (logEnable) log.debug "skip: ${cmd}"
}

/*
 ***********************
 * WindowShade methods *
 ***********************
 */
def close() {
   if (logEnable) log.debug "close()"
   // def cmd = zwave.switchBinaryV1.switchBinarySet(switchValue: 0xFF)
   // zwaveSecureEncap(cmd.format())
}

def open() {
   if (logEnable) log.debug "open()"
   // def cmd = zwave.switchBinaryV1.switchBinarySet(switchValue: 0x00)
   // zwaveSecureEncap(cmd.format())
}

def setPosition(Number position) {
   if (logEnable) log.debug "setPosition(${position})"
}

def startPositionChange(String direction) {
    // direction is "open" or "close"
   if (logEnable) log.debug "startPositionChange(${direction})"
}

def stopPositionChange() {
   if (logEnable) log.debug "stopPositionChange()"
}
