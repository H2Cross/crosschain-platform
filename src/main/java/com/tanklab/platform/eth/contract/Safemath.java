package com.tanklab.platform.eth.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple10;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 5.0.0.
 */
@SuppressWarnings("rawtypes")
public class Safemath extends Contract {
    public static final String BINARY = "608060405234801561000f575f80fd5b50611a2c8061001d5f395ff3fe608060405234801561000f575f80fd5b5060043610610055575f3560e01c806338fbba1f14610059578063a3eeb5191461008a578063a8d4efd9146100c3578063b0c8f9dc146100f3578063b65dbe9414610123575b5f80fd5b610073600480360381019061006e9190610f4d565b610153565b604051610081929190611026565b60405180910390f35b6100a4600480360381019061009f9190610f4d565b610507565b6040516100ba9a9998979695949392919061106c565b60405180910390f35b6100dd60048036038101906100d89190610f4d565b61080c565b6040516100ea9190611143565b60405180910390f35b61010d60048036038101906101089190610f4d565b610841565b60405161011a919061115c565b60405180910390f35b61013d6004803603810190610138919061119f565b6108c4565b60405161014a9190611320565b60405180910390f35b60605f805f84604051610166919061137a565b9081526020016040518091039020604051806101400160405290815f8201548152602001600182018054610199906113bd565b80601f01602080910402602001604051908101604052809291908181526020018280546101c5906113bd565b80156102105780601f106101e757610100808354040283529160200191610210565b820191905f5260205f20905b8154815290600101906020018083116101f357829003601f168201915b50505050508152602001600282018054610229906113bd565b80601f0160208091040260200160405190810160405280929190818152602001828054610255906113bd565b80156102a05780601f10610277576101008083540402835291602001916102a0565b820191905f5260205f20905b81548152906001019060200180831161028357829003601f168201915b505050505081526020016003820180546102b9906113bd565b80601f01602080910402602001604051908101604052809291908181526020018280546102e5906113bd565b80156103305780601f1061030757610100808354040283529160200191610330565b820191905f5260205f20905b81548152906001019060200180831161031357829003601f168201915b50505050508152602001600482018054610349906113bd565b80601f0160208091040260200160405190810160405280929190818152602001828054610375906113bd565b80156103c05780601f10610397576101008083540402835291602001916103c0565b820191905f5260205f20905b8154815290600101906020018083116103a357829003601f168201915b50505050508152602001600582015481526020016006820154815260200160078201";

    public static final String FUNC_ADD = "add";

    public static final String FUNC_BEEF = "beef";

    public static final String FUNC_IS_BEEF = "is_beef";

    public static final String FUNC_QUERYBEEF = "queryBeef";

    public static final String FUNC_SELLBEEF = "sellBeef";

    @Deprecated
    protected Safemath(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Safemath(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Safemath(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Safemath(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<TransactionReceipt> add(String beef_key) {
        final Function function = new Function(
                FUNC_ADD, 
                Arrays.<Type>asList(new Utf8String(beef_key)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple10<BigInteger, String, String, String, String, BigInteger, BigInteger, String, BigInteger, byte[]>> beef(String param0) {
        final Function function = new Function(FUNC_BEEF, 
                Arrays.<Type>asList(new Utf8String(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}));
        return new RemoteFunctionCall<Tuple10<BigInteger, String, String, String, String, BigInteger, BigInteger, String, BigInteger, byte[]>>(function,
                new Callable<Tuple10<BigInteger, String, String, String, String, BigInteger, BigInteger, String, BigInteger, byte[]>>() {
                    @Override
                    public Tuple10<BigInteger, String, String, String, String, BigInteger, BigInteger, String, BigInteger, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple10<BigInteger, String, String, String, String, BigInteger, BigInteger, String, BigInteger, byte[]>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (String) results.get(7).getValue(), 
                                (BigInteger) results.get(8).getValue(), 
                                (byte[]) results.get(9).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Boolean> is_beef(String param0) {
        final Function function = new Function(FUNC_IS_BEEF, 
                Arrays.<Type>asList(new Utf8String(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Tuple2<String, byte[]>> queryBeef(String beef_key) {
        final Function function = new Function(FUNC_QUERYBEEF, 
                Arrays.<Type>asList(new Utf8String(beef_key)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Bytes32>() {}));
        return new RemoteFunctionCall<Tuple2<String, byte[]>>(function,
                new Callable<Tuple2<String, byte[]>>() {
                    @Override
                    public Tuple2<String, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<String, byte[]>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> sellBeef(String beef_key, BigInteger beef_id, String transfer_time, String transfer_place, String transfer_person, String sell_place, BigInteger price, BigInteger weight, String quality_guarantee_time, BigInteger ox_id) {
        final Function function = new Function(
                FUNC_SELLBEEF, 
                Arrays.<Type>asList(new Utf8String(beef_key),
                new Uint256(beef_id),
                new Utf8String(transfer_time),
                new Utf8String(transfer_place),
                new Utf8String(transfer_person),
                new Utf8String(sell_place),
                new Uint256(price),
                new Uint256(weight),
                new Utf8String(quality_guarantee_time),
                new Uint256(ox_id)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Safemath load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Safemath(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Safemath load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Safemath(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Safemath load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Safemath(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Safemath load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Safemath(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Safemath> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Safemath.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Safemath> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Safemath.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<Safemath> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Safemath.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Safemath> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Safemath.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}
