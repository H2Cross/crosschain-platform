// SPDX-License-Identifier: GPL-3.0

pragma solidity ^0.8.0;

contract Abroad {

    //牛    测试用例  ： cow1,1,good,20230727,30,200,American,John      (ox_key使用keccak256对牛的信息进行hash得到)
    struct  Cow {
        uint256 ox_id;  //牛的id
        string breed;   //牛的品种
        string end_time;   //出栏时间
        uint256 feeding_period;  //出栏周期
        uint256 weight;          //体重
        string location;    //饲养地
        string feed_person;   //饲养负责人
        bytes32  ox_hash;      //每头牛对应的hash
    }

    mapping(string =>  Cow) public cattle;

    function addCow(
        string memory ox_key,
        uint256 ox_id,
        string memory breed,
        string memory end_time,
        uint256 feeding_period,
        uint256 weight,
        string memory location,
        string memory feed_person
    ) public {
        bytes32 ox_hash = keccak256(
            abi.encodePacked(
                ox_id,
                breed,
                end_time,
                feeding_period,
                weight,
                location,
                feed_person
            )
        );
        cattle[ox_key] = Cow(
            ox_id,
            breed,
            end_time,
            feeding_period,
            weight,
            location,
            feed_person,
            ox_hash
        );
    }
    

    function queryCow(string memory ox_key) public view returns(string memory, bytes32) {
        Cow memory cow = cattle[ox_key];
        return (string(abi.encodePacked(uintToString(cow.ox_id), "/", 
                        cow.breed, "/", 
                        cow.end_time, "/",
                        uintToString(cow.feeding_period), "/",
                        uintToString(cow.weight), "/",
                        cow.location, "/",
                        cow.feed_person)), cow.ox_hash);
    }

    //牛肉  测试用例  ： meet1,1001,good,20230727,American,John,20230728,NewYork,Jack,1      (beef_hash使用keccak256对牛肉的信息进行hash得到)
    struct Beef {
        uint256 beef_id; //牛肉id
        string breed;   //牛肉品种
        string process_time;   //加工时间
        string process_place;  //加工地点
        string process_person;       //加工负责人
        string transport_time;    //转运时间
        string transport_place;   //转运地
        string transport_person;   //运输负责人
        uint256 ox_id;            //对应的牛的id
        bytes32 beef_hash;         //每头肉对应的hash
    }

    mapping(string => Beef) public beef;

    function addBeef(
        string memory beef_key,
        uint256 beef_id,
        string memory breed,   
        string memory process_time,   
        string memory process_place,  
        string memory process_person,       
        string memory transport_time,    
        string memory transport_place,   
        string memory transport_person,   
        uint256 ox_id            
    ) public {
        bytes32 beef_hash = keccak256(
            abi.encodePacked(
                beef_id,
                breed,
                process_time,
                process_place,
                process_person,
                transport_time,
                transport_place,
                transport_person,
                ox_id
            )
        );
        beef[beef_key] = Beef(
            beef_id,
            breed,
            process_time,
            process_place,
            process_person,
            transport_time,
            transport_place,
            transport_person,
            ox_id,
            beef_hash
        );
    }

    function queryBeef(string memory beef_key) public view returns(string memory, bytes32) {
        Beef memory bf = beef[beef_key];
        return (string(abi.encodePacked(uintToString(bf.beef_id), "/", 
                        bf.breed, "/", 
                        bf.process_time, "/",
                        bf.process_place, "/",
                        bf.process_person, "/",
                        bf.transport_time, "/",
                        bf.transport_place, "/",
                        bf.transport_person, "/",
                        uintToString(bf.ox_id))), bf.beef_hash);
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