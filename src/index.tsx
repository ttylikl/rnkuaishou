import { DeviceEventEmitter, NativeModules } from 'react-native';
import { EventEmitter } from 'events';

type RnkuaishouType = {
  multiply(a: number, b: number): Promise<number>;
  dyauth(): Promise<string>;
  registerApp(appId: string, universallink: string): Promise<string>;
  sendAuthRequest(): Promise<any>;
  foo(): Promise<string>;
};

const { Rnkuaishou } = NativeModules;

// Event emitter to dispatch request and response from WeChat.
const emitter = new EventEmitter();

DeviceEventEmitter.addListener('KuaiShou_Resp', (resp) => {
  console.log('KuaiShou_Resp', resp);
  emitter.emit(resp.type, resp);
});

Rnkuaishou.foo = (): Promise<any> => {
  return new Promise((resolve) => {
    return resolve('okokok');
  });
};

Rnkuaishou.sendAuthRequest = (loginType: String = "app"): Promise<any> => {
  return new Promise((resolve, reject) => {
    emitter.once('SendAuth.Resp', (resp) => {
      console.log('SendAuth.Resp', resp);
      if (resp.authCode) {
        resolve(resp);
      } else {
        reject(resp);
      }
    });
    Rnkuaishou.ksauth(loginType);
  });
};

export default Rnkuaishou as RnkuaishouType;
