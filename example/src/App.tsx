import * as React from 'react';
import { StyleSheet, View, Text, TouchableOpacity } from 'react-native';
import Rnkuaishou from 'rnkuaishou';

export interface Props {
  name: string;
}
export interface State {
  result: number;
  msg: string;
}

export default class Home extends React.Component<Props, State> {
  constructor(props: any) {
    super(props);
  }

  state: State = {
    result: 0,
    msg: '',
  };

  componentDidMount() {}

  onTest1 = async (e: any) => {
    console.log('onTest1', e);
    let result: number = await Rnkuaishou.multiply(3, 7);
    this.setState({ result });
  };

  onTest2 = async (e: any) => {
    console.log('onTest2', e);
    let msg: string = await Rnkuaishou.foo();
    this.setState({ msg });
  };

  onTest3 = async (e: any) => {
    console.log('onTest3', e);
    let msg: string = await Rnkuaishou.registerApp(
      'ks675258470891385408',
      'https://www.zbz666.com/'
    ); // 申请完成后替换
    console.log('registerApp', msg);
    this.setState({ msg });
    let r = await Rnkuaishou.sendAuthRequest();
    console.log('sendAuth:', r);
    msg = msg + '\n' + JSON.stringify(r);
    this.setState({ msg });
  };

  render() {
    return (
      <View style={styles.container}>
        <Text>Home</Text>
        <Text>Result: {this.state.result}</Text>
        <Text>Message: {this.state.msg}</Text>
        <TouchableOpacity onPress={this.onTest1}>
          <Text>Test #1</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onTest2}>
          <Text>Test #2</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={this.onTest3}>
          <Text>Test #3(init KuaiShou)</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 15,
    backgroundColor: '#F5FCFF',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
