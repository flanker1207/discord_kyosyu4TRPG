package org.flan.discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DiscordMessageListener extends ListenerAdapter{

    // 卓募集フラグ
    private boolean isCalling = false;
    // GMChoiceフラグ
    private boolean isGMChoice = false;
    // 抽選フラグ
    private boolean isRandom = false;

    // 募集上限数
    private int maxNumber = 0;
    // 挙手集
    private int handsUpNumber = 0;

    private List<String> handsUpMembers = new ArrayList();

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {

        // メッセージ取得
        String msg = e.getMessage().getContentRaw(); //入力されたメッセージを取得
        // メッセージの中身が空なら無視する
        if(msg.isEmpty()){
            return;
        }
        // botの発言は無視する
        if (!e.getAuthor().isBot()) {
            // メッセージから余計な箇所を削除
            msg = msg.substring(msg.indexOf(">")+1).trim();

            if(msg.startsWith("挙手")){
                // フォーマットチェック
                if(getMaxNumber(msg) < 1){
                    // 不正なフォーマットの場合はメッセージを出す
                    e.getTextChannel().sendMessage("参加上限人数の指定が不正です").queue();
                    return;
                }
                // 卓募集中フラグが立っていないか確認
                if(isCalling || isGMChoice){
                    e.getTextChannel().sendMessage("既に卓募集中です").queue();;
                    return;
                }
                if(msg.contains("抽選")){
                    // ランダム抽選フラグを立てる
                    isRandom = true;
                }
                // 上限数に募集数を淹れる
                maxNumber = getMaxNumber(msg);
                // 卓募集中フラグを立てる
                isCalling = true;
                // 卓募集開始メッセージ送信
                e.getTextChannel().sendMessage("卓募集が開始されました！\n" + msg).queue();
            } else if (msg.startsWith("GMChoice")) {
                // 卓募集中フラグが立っていないか確認
                if(isCalling || isGMChoice){
                    e.getTextChannel().sendMessage("既に卓募集中です").queue();;
                    return;
                }
                int maxNum = getChoiceMaxNumber(msg);
                if(maxNum < 1){
                    e.getTextChannel().sendMessage("参加上限人数の指定が不正です").queue();
                    return;
                } else if (maxNum > 3) {
                    e.getTextChannel().sendMessage("GMChoiceは人数は3人までです").queue();
                    return;
                }
                // GMChoiceフラグを立てる
                isGMChoice = true;
                // 上限数に募集数を淹れる
                maxNumber = maxNum;
                // GMChoice開始メッセージを送信
                e.getTextChannel().sendMessage("GMChoiceが開始されました！\n" + msg).queue();
            } else if (msg.startsWith("ノ")) {
                // 卓募集フラグが立っているか確認
                if(!(isCalling || isGMChoice)){
                    e.getTextChannel().sendMessage("卓募集はありません").queue();;
                    return;
                }
                // 既に挙手リストに名前が無いか確認
                if(handsUpMembers.contains(e.getAuthor().getId())){
                    e.getTextChannel().sendMessage("既に挙手しています").queue();;
                    return;
                };
                // 挙手数に+1
                handsUpNumber++;
                // 挙手リストに挙手者情報を追加
                handsUpMembers.add(e.getAuthor().getName());
                // 抽選ではなく、挙手上限に達した場合
                if(!isRandom && maxNumber <= handsUpNumber){
                    // 卓参加者をリスト化
                    List<String> list = getWinnerNames();
                    String winnerMsg = "参加者：";
                    for(int i = 0; i < list.size(); i++){
                        winnerMsg = winnerMsg + list.get(i) + " ";
                    }
                    // 終了処理を呼び出す。
                    clearFlags();
                    e.getTextChannel().sendMessage("卓募集を締めきりました\n" + winnerMsg).queue();
                }
            }else if(msg.startsWith("へ")){
                if(handsUpMembers.contains(e.getAuthor().getName())){
                    handsUpMembers.remove(handsUpMembers.indexOf(e.getAuthor().getName()));
                    e.getTextChannel().sendMessage("挙手を取り下げました").queue();;
                    return;
                }
            } else if (msg.startsWith("〆")) {
                // 卓募集フラグが立っているか確認
                if(!(isCalling || isGMChoice)){
                    e.getTextChannel().sendMessage("卓募集はありません").queue();;
                    return;
                }
                // 卓参加者をリスト化
                List<String> list = getWinnerNames();
                String winnerMsg = "参加者：";
                for(int i = 0; i < list.size(); i++){
                    winnerMsg = winnerMsg + list.get(i) + " ";
                }
                // 終了処理を呼び出す。
                clearFlags();
                e.getTextChannel().sendMessage("卓募集を締めきりました\n" + winnerMsg).queue();
            }
        }
    }

    /*卓の参加者のリストを返す*/
    private List<String> getWinnerNames(){
        List<String> winnerList = new ArrayList<>();
        if(isRandom && maxNumber < handsUpNumber){

            Random random = new Random();
            for(int i = 0; i < maxNumber; i++){
                int randomIndex = random.nextInt(handsUpMembers.size());
                winnerList.add(handsUpMembers.get(randomIndex));
                handsUpMembers.remove(randomIndex);
            }
        }else {
            handsUpMembers.forEach(name -> winnerList.add(name));
        }
        return  winnerList;
    }

    /*〆の時の処理*/
    private void clearFlags(){
        isCalling = false;
        isGMChoice = false;
        isRandom = false;
        handsUpNumber = 0;
        handsUpMembers.clear();
    }

    /*挙手時の参加上限人数を取得する
    * フォーマット挙手{n} 例：「挙手5 ～説明～」*/
    private int getMaxNumber(String msg) {
        char dst[] = new char[1];
        // 3文字目を固定で取得する
        msg.getChars(2, 3, dst  ,0);
        // 3文字目を数字に変換
        int num = Character.getNumericValue(dst[0]);
        // 1以下なら無効な値の為返却
        if(num < 1){
            return num;
        }
        int result = num;
        // 2桁募集があるかもなので念のため4文字目も取得する
        msg.getChars(3, 4, dst  ,0);
        // 4文字目を数字に変換
        int num2 = Character.getNumericValue(dst[0]);
        // 数字の場合は3文字目を10の桁に直して足す
        if(num2 > 0){
           result  = num * 10 + num2;
        }
        return result;
    }

    /*挙手時の参加上限人数を取得する
     * フォーマット挙手{n} 例：「挙手5 ～説明～」*/
    private int getChoiceMaxNumber(String msg) {
        char dst[] = new char[1];
        // 3文字目を固定で取得する
        msg.getChars(2, 3, dst  ,0);
        // 3文字目を数字に変換
        int num = Character.getNumericValue(dst[0]);
        return num;
    }
}
