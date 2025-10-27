// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.0;
//千万不能改这个合约名啊
contract safemath {
    //牛肉的销售信息  测试用例  ： meet1,1001,20230728,China,ZhangSan,Tianin,35,20,20230910,1     (transaction_hash使用keccak256对牛肉的售卖信息进行hash得到)
    struct Beef {
        uint256 beef_id;     //牛肉的id
        string transfer_time;    //转运时间
        string transfer_place;   //转运地
        string transfer_person;   //运输负责人
        string sell_place;         //国内售卖地
        uint256 price;                     //售卖价格
        uint256 weight;                    //净重
        string  quality_guarantee_time;    //保质期(过期时间)
        uint256 ox_id;            //对应的牛的id
        bytes32 transaction_hash;         //每块肉售卖对应的hash
    }

    mapping(string => Beef) public beef;
    mapping(string => bool) public is_beef;

    function add(string memory beef_key) public returns(uint256) {
        uint256 answer;
        if(is_beef[beef_key]==true) {
            answer=1;
        } 
        else{
            is_beef[beef_key]=true;
            answer=0;
        }
        return answer;
    }

    function sellBeef(
        string memory beef_key,
        uint256 beef_id,
        string memory transfer_time,   
        string memory transfer_place,   
        string memory transfer_person,   
        string memory sell_place,         
        uint256 price,                    
        uint256 weight,                  
        string memory quality_guarantee_time,   
        uint256 ox_id                  
    ) public returns(string memory){

        if(!is_beef[beef_key]){
            return "beef_key is wrong or not be added";
        }

        bytes32 transaction_hash = keccak256(
            abi.encodePacked(
                beef_id,
                transfer_time,   
                transfer_place,   
                transfer_person,   
                sell_place,         
                price,                    
                weight,                  
                quality_guarantee_time,   
                ox_id
            )
        );
        beef[beef_key] = Beef(
                beef_id,
                transfer_time,   
                transfer_place,   
                transfer_person,   
                sell_place,         
                price,                    
                weight,                  
                quality_guarantee_time,   
                ox_id,
                transaction_hash
        );
        return "sellbeef successful";
    }

    function queryBeef(string memory beef_key) public view returns(string memory, bytes32) {
        Beef memory bf = beef[beef_key];
        return (string(abi.encodePacked(uintToString(bf.beef_id), "/", 
                        bf.transfer_time, "/", 
                        bf.transfer_place, "/",
                        bf.transfer_person, "/",
                        bf.sell_place, "/",
                        uintToString(bf.price), "/",
                        uintToString(bf.weight), "/",
                        bf.quality_guarantee_time, "/",
                        uintToString(bf.ox_id))), bf.transaction_hash);
    }

    //这个函数用来连接两个字符串 'aaa' + 'bbb' =>  'aaabbb'
    function strConcat(string memory _a, string memory _b) private pure returns (string memory){
        bytes memory _ba = bytes(_a);
        bytes memory _bb = bytes(_b);
        string memory ret = new string(_ba.length + _bb.length);
        bytes memory bret = bytes(ret);
        uint k = 0;
        for (uint i = 0; i < _ba.length; i++) bret[k++] = _ba[i];
        for (uint i = 0; i < _bb.length; i++) bret[k++] = _bb[i];
        return string(ret);
    }    
    
    //这个函数最关键，比较取巧，用来将uint256类型的 0-9 数字转成字符
    function toStr(uint256 value) private pure returns(string memory) {
        bytes memory alphabet = "0123456789abcdef";
        //这里把数字转成了bytes32类型，但是因为我们知道数字是 0-9 ，所以前面其实都是填充了0
        bytes memory data = abi.encodePacked(value);
        bytes memory str = new bytes(1);
        //所以最后一位才是真正的数字
        uint i = data.length - 1;
        str[0] = alphabet[uint(uint8(data[i] & 0x0f))];
        return string(str);
    }

    //调用这个函数，通过取模的方式，一位一位转换
    function uintToString(uint _uint) private pure returns (string memory str) {
 
        if(_uint==0) return '0';
 
        while (_uint != 0) {
            //取模
            uint remainder = _uint % 10;
            //每取一位就移动一位，个位、十位、百位、千位……
            _uint = _uint / 10;
            //将字符拼接，注意字符位置
            str = strConcat(toStr(remainder),str);
        }
    }
}