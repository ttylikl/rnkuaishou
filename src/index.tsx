import { NativeModules } from 'react-native';

type RnkuaishouType = {
  multiply(a: number, b: number): Promise<number>;
};

const { Rnkuaishou } = NativeModules;

export default Rnkuaishou as RnkuaishouType;
