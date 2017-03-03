import React, { Component } from 'react';
import {
  NativeModules,
  DeviceEventEmitter,
  Platform,
  requireNativeComponent,
  NativeAppEventEmitter
} from 'react-native';
var location = function getLocation(successEvents,errorEvents){
     NativeModules.MapMoudle.getCurrentPosition((position) => successEvents(position), 
     (error) => errorEvents(error));
}
var tempSuccessEvent = {};
var tempErrorEvents = {};
var listener =  function onListenerLocationChang(successEvents,errorEvents){
    tempSuccessEvent = successEvents;
    tempErrorEvents = errorEvents;
     NativeModules.MapMoudle.startObserving();
      DeviceEventEmitter.addListener('locationDidChange',successEvents);
      DeviceEventEmitter.addListener('locationError',errorEvents);
}
var stoplistener = function stopLocationListener(){
     NativeModules.MapMoudle.stopObserving();
     DeviceEventEmitter.removeListener('locationDidChange',tempSuccessEvent);
     DeviceEventEmitter.removeListener('locationError',tempErrorEvents);
}
const Api = {
  get getLocation() { return location; },
  get onListenerLocationChang() { return listener; },
  get stopLocationListener() { return stoplistener; },
  get BaduMapView() { return NativeModules.MapMoudle; },
}

if (Platform.OS === 'ios') {
	module.exports = requireNativeComponent('RCTCustomMapView',null);
} else {
	module.exports = Api;
}




