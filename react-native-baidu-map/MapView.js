import React, { Component,PropTypes } from 'react';
import {
  requireNativeComponent,
  View
} from 'react-native';

let iface={
    name:'RCTBaiduMap',
    propTypes:{
        mapType :                      PropTypes.number,
        zoomLevel:                     PropTypes.number,
        showsUserLocation:             PropTypes.bool,
        showsCompass:                  PropTypes.bool,
        zoomEnabled:                   PropTypes.bool,
        rotateGesturesEnabled:         PropTypes.bool,
        scrollGesturesEnabled:         PropTypes.bool,
        allGesturesEnabled:            PropTypes.bool,
        region: React.PropTypes.shape({
            latitude: React.PropTypes.number.isRequired,
            longitude: React.PropTypes.number.isRequired,
        }),
        annotations: React.PropTypes.arrayOf(React.PropTypes.shape({
            latitude: React.PropTypes.number.isRequired,
            longitude: React.PropTypes.number.isRequired,
        })),
         ...View.propTypes
    },
};
//module.exports = requireNativeComponent('RCTBaiduMap',iface);
var  RCTBaiduMap = requireNativeComponent('RCTBaiduMap',iface);

class MyView extends Component{
    static propTypes:{
         onChangeMessage:PropTypes.func,
    }
    constructor(props){
        super();
        this._onChange = this._onChange.bind(this);
    }
    _onChange(event:Event){
       
        if(!this.props.onChangeMessage){
            alert("retrun");
            return;
        }
        if(event.nativeEvent.message ==='position'){
            this.props.onChangeMessage(event.nativeEvent.message);
        }
    }
    render(){
        return(
            <RCTBaiduMap {...this.props} onChange={this._onChange}/>
        );
    }
}
module.exports = MyView;
