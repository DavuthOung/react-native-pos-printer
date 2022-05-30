import * as React from 'react';

import { 
  StyleSheet,
  View,
  Button,
  ScrollView,
  TouchableOpacity,
  Text 
} from 'react-native';
import { 
  enableBluetooth ,
  disableBluetooth, 
  connect,
  getDevicePaired,
  initializeBluetooth,
  printPic,
  setBlob,
  printerAlign
} from 'react-native-pos-printer';
import ViewShot from "react-native-view-shot";
// let RNFS = require('react-native-fs');
import RNFS from 'react-native-fs';
export default class App extends React.Component{
  state = {
    list:  []
  }

  componentDidMount(){
    initializeBluetooth("DC:0D:30:87:24:11").then((result: any) => {
      // console.log(result);
    })
  }

  render() {
   
    return (
    <View style={styles.container}>
     <Button
        title="Enable"
        onPress={async () => {
          const result = await enableBluetooth();
          // const list = result.map((item: any) => JSON.parse(item))
          console.log(result)
        
        }}
      />
      <Button
        title="Disable"
        onPress={() => {
          disableBluetooth().then(result => {
            console.log(result);
          });
        }}
      />

      <Button
        title="Device paired"
        onPress={() => {
          getDevicePaired().then((result: any) => {
            const list = result.map((item: any) => JSON.parse(item))
            console.log(list);
            this.setState({list})
          });
        }}
      />

      <Button
        title="print"
        onPress={() => {
          this.refs.viewShot.capture().then( async(uri: any) => {
            const image = await RNFS.readFile(uri, 'base64');            
            await printerAlign(1);
            await setBlob(0);
            await printPic(image,{width: 384,left: 0});
          });
          
        }}
      />

      <ScrollView contentContainerStyle={{flexGrow: 1}}>
        {
            this.state.list.map((item: any,index: number) => (
              <TouchableOpacity key={index} style={{padding: 10}} onPress={() => {
                connect(item.address).then((result: any) => {
                    console.log(result);
                })
              }}>
                <Text>{item.name}</Text>
                <Text>{item.address}</Text>
              </TouchableOpacity>
            ))
          }
      
      </ScrollView>
      <ViewShot 
        ref="viewShot"
        options={{quality: 0.1}}
        style={{backgroundColor: '#fff',height: 300}}>
          <View style={{flex: 1,justifyContent: 'center',alignItems: 'center'}}>
            <Text>Testing</Text>
          </View>
      </ViewShot>
    </View>
  );}
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  
  }
});
