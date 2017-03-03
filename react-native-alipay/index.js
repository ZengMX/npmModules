import React, { Component } from 'react';
import {
  NativeModules,
  DeviceEventEmitter,
  Platform,
  NativeAppEventEmitter
} from 'react-native';
export function pay(info){
  if(Platform.OS === 'ios') {
     NativeModules.AliPay.pay(info);
  } else {
    NativeModules.PlayModule.pay(info);
  }
}
export function payBlock(fireEvents){
  if(Platform.OS === 'ios') {
    NativeAppEventEmitter.addListener(
            'sendPlayResultMessage',
            (reminder) => fireEvents(reminder)
        );
  } else {
    DeviceEventEmitter.addListener(
            'sendPlayResultMessage',
            (reminder) =>{
                fireEvents(reminder);
            } 
      );
  }
}