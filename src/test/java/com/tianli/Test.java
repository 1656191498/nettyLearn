package com.tianli;

import com.mmorrell.openbook.manager.OpenBookManager;
import com.mmorrell.openbook.model.OpenBookMarket;
import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Test {
    public static void main(String[] args) throws RpcException {
//        RpcClient client = new RpcClient(Cluster.MAINNET);
        RpcClient client = new RpcClient("http://135.181.229.238:8899");
        OpenBookManager openBookManager = new OpenBookManager(client);
        List<OpenBookMarket> openBookMarkets = openBookManager.getOpenBookMarkets();
        log.info("Market cache: {}", openBookMarkets);
//        Transaction transaction = new Transaction();
//        SystemProgram.transfer()
//        new TransactionInstruction()
//        transaction.addInstruction()
//        client.getApi().sendTransaction()
//        long balance = client.getApi().getBalance(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"));
//        System.out.println(balance);
//        final PublicKey solUsdcPublicKey = new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6");

//        final Market solUsdcMarket = new Op()
//                .setClient(new RpcClient())
//                .setPublicKey(solUsdcPublicKey)
//                .setRetrieveOrderBooks(true)
//                .build();
//
//        SubscriptionWebSocketClient instance = SubscriptionWebSocketClient.getInstance(Cluster.MAINNET.getEndpoint());
//        instance.logsSubscribe("srmqPvymJeFKQ4zGQed1GFppgkRHL9kaELCbyksJtPX", message -> {
//            if (message != null) {
//                AbstractMap<String, String> map = (AbstractMap)message;
//                if (map.get("err")==null) {
//                    System.out.println(message.toString());
//                }
//
//            }
//
//        });
//        final Market solUsdcMarket = new MarketBuilder()
//                .setClient(client)
//                .setPublicKey(solUsdcPublicKey)
//                .setRetrieveOrderBooks(true)
//                .build();
//        final OrderBook bids = solUsdcMarket.getBidOrderBook();
//        System.out.println(bids.getBestBid().toString());
    }

}
