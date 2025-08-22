package com.tanklab.platform.eth;

import org.web3j.codegen.SolidityFunctionWrapperGenerator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;


public class SolidityGenerateUtil {
    // 私有实例，类初始化就加载
    private static SolidityGenerateUtil solidityGenerateUtil = new SolidityGenerateUtil();

    // 私有构造方法
    private SolidityGenerateUtil() {}

    // 公共的、静态的获取实例方法
    public static SolidityGenerateUtil getInstance() {
        return solidityGenerateUtil;
    }

    public static void genAbiAndBin(){
        String abi  = "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"beef_key\",\"type\":\"string\"}],\"name\":\"add\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"name\":\"beef\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"beef_id\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"transfer_time\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"transfer_place\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"transfer_person\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"sell_place\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"price\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"weight\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"quality_guarantee_time\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"ox_id\",\"type\":\"uint256\"},{\"internalType\":\"bytes32\",\"name\":\"transaction_hash\",\"type\":\"bytes32\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"name\":\"is_beef\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"beef_key\",\"type\":\"string\"}],\"name\":\"queryBeef\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"beef_key\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"beef_id\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"transfer_time\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"transfer_place\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"transfer_person\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"sell_place\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"price\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"weight\",\"type\":\"uint256\"},{\"internalType\":\"string\",\"name\":\"quality_guarantee_time\",\"type\":\"string\"},{\"internalType\":\"uint256\",\"name\":\"ox_id\",\"type\":\"uint256\"}],\"name\":\"sellBeef\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
        String bin = "608060405234801561000f575f80fd5b50611a2c8061001d5f395ff3fe608060405234801561000f575f80fd5b5060043610610055575f3560e01c806338fbba1f14610059578063a3eeb5191461008a578063a8d4efd9146100c3578063b0c8f9dc146100f3578063b65dbe9414610123575b5f80fd5b610073600480360381019061006e9190610f4d565b610153565b604051610081929190611026565b60405180910390f35b6100a4600480360381019061009f9190610f4d565b610507565b6040516100ba9a9998979695949392919061106c565b60405180910390f35b6100dd60048036038101906100d89190610f4d565b61080c565b6040516100ea9190611143565b60405180910390f35b61010d60048036038101906101089190610f4d565b610841565b60405161011a919061115c565b60405180910390f35b61013d6004803603810190610138919061119f565b6108c4565b60405161014a9190611320565b60405180910390f35b60605f805f84604051610166919061137a565b9081526020016040518091039020604051806101400160405290815f8201548152602001600182018054610199906113bd565b80601f01602080910402602001604051908101604052809291908181526020018280546101c5906113bd565b80156102105780601f106101e757610100808354040283529160200191610210565b820191905f5260205f20905b8154815290600101906020018083116101f357829003601f168201915b50505050508152602001600282018054610229906113bd565b80601f0160208091040260200160405190810160405280929190818152602001828054610255906113bd565b80156102a05780601f10610277576101008083540402835291602001916102a0565b820191905f5260205f20905b81548152906001019060200180831161028357829003601f168201915b505050505081526020016003820180546102b9906113bd565b80601f01602080910402602001604051908101604052809291908181526020018280546102e5906113bd565b80156103305780601f1061030757610100808354040283529160200191610330565b820191905f5260205f20905b81548152906001019060200180831161031357829003601f168201915b50505050508152602001600482018054610349906113bd565b80601f0160208091040260200160405190810160405280929190818152602001828054610375906113bd565b80156103c05780601f10610397576101008083540402835291602001916103c0565b820191905f5260205f20905b8154815290600101906020018083116103a357829003601f168201915b50505050508152602001600582015481526020016006820154815260200160078201";
        String abiFileName = "safemath.abi";
        String binFileName = "safemath.bin";
        generateAbiAndBin(abi,bin,abiFileName,binFileName);
    }

    public static void generateAbiAndBin(String abi,String bin,String abiFileName,String binFileName){
        File abiFile = new File("src/main/java/com/tanklab/platform/eth/contract/"+abiFileName);
        File binFile = new File("src/main/java/com/tanklab/platform/eth/contract/"+binFileName);
        BufferedOutputStream abiBos = null;
        BufferedOutputStream binBos = null;
        try{
            FileOutputStream abiFos = new FileOutputStream(abiFile);
            FileOutputStream binFos = new FileOutputStream(binFile);
            abiBos = new BufferedOutputStream(abiFos);
            binBos = new BufferedOutputStream(binFos);
            abiBos.write(abi.getBytes());
            abiBos.flush();
            binBos.write(bin.getBytes());
            binBos.flush();
            generateJavaFile(abiFile.getPath(),binFile.getPath());
        }catch (Exception e){
            e.printStackTrace();

        }finally {
            if(abiBos != null){
                try{
                    abiBos.close();;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(binBos != null){
                try {
                    binBos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void generateJavaFile(String abiFile, String binFile){
        String generateFile = "src/main/java/com/tanklab/platform/eth/contract/";
        generateClass(abiFile,binFile,generateFile);
    }

    public static void generateClass(String abiFile,String binFile,String generateFile){
        String[] args = Arrays.asList(
                "-a",abiFile,
                "-b",binFile,
                "-p","",
                "-o",generateFile
        ).toArray(new String[0]);
        Stream.of(args).forEach(System.out::println);
        SolidityFunctionWrapperGenerator.main(args);
    }

    public static void main(String[] args) {
        genAbiAndBin();
    }

}
