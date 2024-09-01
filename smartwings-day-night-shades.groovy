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
    capability "Battery"
    capability "WindowShade"
    capability "Refresh"

    command "topRailPosition", [
			[name:"Position*", description:"Final position", type: "NUMBER"]
    ]
    command "bottomRailPosition", [
			[name:"Position*", description:"Final position", type: "NUMBER"]
    ]
  }

  preferences {
    input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
  }
}

def installed() {
  log.debug "installed()"
  refresh()
}

def updated() {
  log.debug "updated()"
  log.warn "debug logging is: ${logEnable == true}"

  if (logEnable) runIn(30 * 60, "logsOff")
}

// handler method for scheduled job to disable debug logging:
void logsOff(){
  log.warn "debug logging disabled..."
  device.updateSetting("logEnable", [value:"false", type:"bool"])
}

def parse(String description) {
  def cmd = zwave.parse(description, commandClassVersions)
  if (cmd) {
    zwaveEvent(cmd)
  }
}

/*
 *****************************
 * zwaveEvent Event Handlers *
 *****************************
 */

def zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
  if (enableDebug) log.debug "BasicReport:  ${cmd}"
}

def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport report) {
  sendEvent(name: "battery", value: report.batteryLevel, unit: "%")
}

def zwaveEvent(hubitat.zwave.commands.switchmultilevelv3.SwitchMultilevelReport report) {
  sendEvent(name: "position", value: report.value, unit: "%")
}

def zwaveEvent(hubitat.zwave.Command cmd) {
  log.debug "Ignoring command: ${cmd}"
}

/*
 ***********************
 * Positioning methods *
 ***********************
 */

def dualRailPosition(Number position) {
  railPosition(0, position)
}

def bottomRailPosition(Number position) {
  railPosition(1, position)
}

def topRailPosition(Number position) {
  railPosition(2, position)
}

def railPosition(Number rail, Number position) {
  position_set_command = zwave.switchMultilevelV3.switchMultilevelSet(
    dimmingDuration: 0, value: position
  )
  cmd = zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint: rail).encapsulate(position_set_command)
    
  zwaveSecureEncap(cmd.format())
}

/*
 *******************
 * Refresh methods *
 *******************
 */
def refresh() {
  log.debug "Refresh()"

  sendEvent(name: "battery", value: nil, descriptionText: "Refreshing", isStateChange: true)
  sendEvent(name: "position", value: nil, descriptionText: "Refreshing", isStateChange: true)

  [
    zwaveSecureEncap(zwave.batteryV1.batteryGet()),
    zwaveSecureEncap(zwave.switchMultilevelV3.switchMultilevelGet()),
  ]
}

/*
 ***********************
 * WindowShade methods *
 ***********************
 */
def close() {
  dualRailPosition(0)
}

def open() {
  dualRailPosition(99)
}

def setPosition(Number position) {
  dualRailPosition(position)
}

// direction is "open" or "close"
def startPositionChange(String direction) {
  if(direction == "open") {
    open()
  }else if(direction == "close") {
    close()
  }
}

def stopPositionChange() {
  def cmd = zwave.switchMultilevelV3.switchMultilevelStopLevelChange()
  zwaveSecureEncap(cmd.format())
}
