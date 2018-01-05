/*
Q：交易成功后没有回调
    1.详情页加入购物车没有回调
    2.只有加入购物车购页加购才有回调
    3.支付的话，如果有接入支付宝sdk，都会有回调的
  ****目前只有通过H5打开页面完成支付，并且集成了alipay的时候才会有支付成功的回调***  （12/7更新）
*/

import React, {PureComponent} from 'react';

import {
    InteractionManager
} from 'react-native';

// 引入sdk
import RNAlibcSdk from 'react-native-alibaichuan';

class Alibaichuan extends PureComponent {
    constructor(props) {
        super(props);
        this.state = {
            ready: false,
            mmpid: 'mm_0000000_0000000_22182111',//三段式
            adzoneid: '22182111',//pid三段式最后一段
            tkkey: '000000',//阿里妈妈那边网站或者app的appkey
            opentype: 'html5',//打开的方式
            isvcode: 'app'//必须传
        };
    }

    componentDidMount() {
        InteractionManager.runAfterInteractions(() => {

        })
    }

    doAlimama = (str) => {
        let that = this;
        if (str.match(/^http[s]?:\/\/.+/)) {
            //是url
            this.showTaokeItemByUrl({
                url: str,
                opentype: 'html5'
            });
        } else {
            this.showTaokeItemById({
                itemid: str
            });
        }
    };

    //显示
    initSDK = () => {
        let that = this;
        return new Promise(function (resolve, reject) {
            RNAlibcSdk.initTae((err) => {
                if (!err)
                    resolve('init success');
                else
                    reject(err);
            });
        });
    };

    showLogin = () => {
        return new Promise(function (resolve, reject) {
            RNAlibcSdk.showLogin((err, userInfo) => {
                if (!err)
                    resolve(userInfo);
                else
                    reject(err);
            })
        });
    };

    isLogin = () => {
        return new Promise(function (resolve, reject) {
            RNAlibcSdk.isLogin((err, isLogin) => {
                if (!err)
                    resolve(isLogin);
                else
                    reject(err);
            })
        });
    };

    getUserInfo = () => {
        return new Promise(function (resolve, reject) {
            RNAlibcSdk.getUserInfo((err, userInfo) => {
                if (!err)
                    resolve(userInfo);
                else
                    reject(err);
            })
        });
    };

    logout = () => {
        return new Promise(function (resolve, reject) {
            RNAlibcSdk.logout((err) => {
                if (!err)
                    resolve('logout success');
                else
                    reject(err);
            })
        });
    };

    showTaokeItemById = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'detail',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    showTaokeItemByUrl = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'url',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    addCartPage = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'addCard',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    shopPage = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'shop',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    myOrdersPage = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'orders',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    myCartsPage = (params) => {
        params = this.mkParam(params);
        RNAlibcSdk.show({
                type: 'mycard',
                payload: params
            }, (err, info) => {
                if (!err)
                    console.log(info)
                else
                    console.log(err)
            }
        );
    };

    //处理参数
    mkParam = (param) => {
        param.mmpid = (param.mmpid && param.mmpid !== '') ? param.mmpid : this.state.mmpid;
        param.adzoneid = (param.adzoneid && param.adzoneid !== '') ? param.adzoneid : this.state.adzoneid;
        param.tkkey = (param.tkkey && param.tkkey !== '') ? param.tkkey : this.state.tkkey;
        param.opentype = (param.opentype && param.opentype !== '') ? param.opentype : this.state.opentype;
        param.isvcode = (param.isvcode && param.isvcode !== '') ? param.isvcode : this.state.isvcode;
        return param;
    };
}

export default Alibaichuan;