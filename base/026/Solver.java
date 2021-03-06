import java.util.*;

class WhatsappService{
  private ArrayList<Chat> rep_chat = new ArrayList<Chat>();
  private ArrayList<User> rep_user = new ArrayList<User>();

  protected User getUser(String userId){
    Boolean userExiste = false;
    for(User user : rep_user){
      if(user.getId().equals(userId)){
        return user;
      }
    }
    System.out.println("fail: User não encontrado!");
    return null;
  }

  protected Chat getChat(String chatId){
    Boolean chatExiste = false;
    for(Chat chat : rep_chat){
      if(chat.getId().equals(chatId)){
        return chat;
      }
    }
    return null;
  }

  public void createChat(String userId, String chatId){
    if(getChat(chatId) != null){
      System.out.println("fail: chat "+ chatId +" ja existe");
      return;
    }
    Chat chat = new Chat(chatId);
    User user = getUser(userId);
    if(user == null){
      return;
    }
    chat.addUserChat(user);
    rep_chat.add(chat);
  }

  public void addByInvite(String guessId, String invitedId, String chatId){
    Chat chat = getChat(chatId);
    User guess = getUser(guessId);
    User invited = getUser(invitedId);
    chat.addByInvite(guess, invited);
  }

  public void createUser(String userId){
    rep_user.add(new User(userId));
  }

  public String allUsers(){
    StringBuilder users = new StringBuilder();
    users.append("[");
    for(User user : rep_user){
      users.append(user.getId() + " ");
    }
    if(users.length() > 1)
      users.deleteCharAt(users.length()-1);
    users.append("]");
    return(users.toString());
  }

  public String getUsersChat(String chatId){
    Chat chat = getChat(chatId);
    StringBuilder users = new StringBuilder();
    users.append("[");
    for(User user : chat.getUsers()){
      users.append(user.getId()+ " ");
    }
    if(users.length() > 1)
      users.deleteCharAt(users.length()-1);
    users.append("]");
    return users.toString();
  }

  public String getChatsUser(String userId){
    User user = getUser(userId);
    StringBuilder chats = new StringBuilder();
    chats.append("[");
    for(Chat chat : user.getChats()){
      chats.append(chat.getId() + " ");
    }
    if(chats.length() > 1)
      chats.deleteCharAt(chats.length()-1);
    chats.append("]");
    return(chats.toString());
  }

  public String getNotifyUser(String userId ){
    User user = getUser(userId);
    StringBuilder chats = new StringBuilder();  
    chats.append("[");
    for(Chat chat : user.getChats()){
            chats.append(chat.getId());
            if(user.getNotifyUser(chat.getId()).getUnreadCout() > 0)
              chats.append("("+ user.getNotifyUser(chat.getId()).getUnreadCout() +") ");
    }
    if(chats.length() > 1)
      chats.deleteCharAt(chats.length()-1);
    chats.append("]");
    return(chats.toString());
  }

  public void removerUserChat(String userId, String chatId){
    Chat chat = getChat(chatId);
    User user = getUser(userId);
    chat.rmUserChat(user);
  }

  public void sendMessage(String userId, String chatId, String message){
    getChat(chatId).deliverZap(getUser(userId), message);
  }

  public String readMessageUserChat(String userId, String chatId){
    User user = getUser(userId);
    Chat chat = getChat(chatId);
    if(user != null && chat != null && chat.hasUser(user)){
      user.getNotifyUser(chatId).rmNotifi();
      return chat.getMsgs(user);
    }else if(chat != null && !chat.hasUser(user))
      return "fail: user "+userId+" nao esta no chat "+chatId;
    else if(chat == null)
      return "fail: chat não encontrado";
    return "fail: chat e user não encontrado";
  }
}

class User{
  private String id;
  protected ArrayList<Chat> chats  = new ArrayList<Chat>();
  protected ArrayList<Notify> notify = new ArrayList<Notify>();

  public User(String nome){
    id = nome;
  }

  public ArrayList<Chat> getChats(){
    return chats;
  }

  public ArrayList<Notify> getNotify(){
    return notify;
  }

  public Notify getNotifyUser(String chat){
    for(Notify noty : notify){
      if(chat.equals(noty.getId()))
        return noty;
    }
    return null;
  }

  public void addChat(Chat chat){
    chats.add(chat);
  }

  public void addNotify(Chat Chat){
    notify.add(new Notify(Chat.getId()));
  }

  public void rmChat(Chat chat){
    chats.remove(chat);
  }

  public String getId(){
    return id;
  }
}

class Notify{
  public String chatId;
  public int  unreadCount;

  public Notify(String chatId){
    this.chatId = chatId;
  }

  public String getId(){
    return chatId;
  }

  public int getUnreadCout(){
    return unreadCount;
  }

  public void addCout(){
    unreadCount++;
  }

  public void rmNotifi(){
    unreadCount = 0;
  }
}

class Chat{
  private String id;
  protected ArrayList<Inbox> inboxes = new ArrayList<Inbox>();
  protected ArrayList<User> users  = new ArrayList<User>();

  public Chat(String idChat){
    id = idChat;
  }

  public String getMsgs(User user){

    StringBuilder msgs = new StringBuilder();

    for(Msg m : getInboxUser(user).getMsgs()){
      msgs.append("["+ m.userId +": "+ m.text+"]\n");
    }
    msgs.deleteCharAt(msgs.length()-1);

    return msgs.toString();
  }

  public ArrayList<User> getUsers(){
    return users;
  }

  public void deliverZap(User userSend, String message){
    for(User user : users){
      if(userSend != user){
        getInboxUser(user).addMsg(userSend.getId(), message);
        user.getNotifyUser(this.id).addCout();
      }
    }
  }

  public Inbox getInboxUser(User user){
    for(Inbox x : inboxes){
      if(user == x.user){
        return x;
      }
    }
    return null;
  }

  public int unreadCount(User user){
    for(Inbox inbox : inboxes){
      if(inbox.user == user){
        return inbox.msgs.size();
      }
    }
    return 0;
  }

  public Boolean hasUser(User user){
    for(User u : users){
      if(u == user) return true;
    }
    return false;
  }

  public void addUserChat(User user){
    users.add(user);
    inboxes.add(new Inbox(user));
    user.addNotify(this);
    user.addChat(this);
  }

  public void addByInvite(User guess, User invited){
    if(hasUser(guess))
      addUserChat(invited);
    else
      System.out.println("fail: user "+ guess.getId() +" não esta no grupo "+ this.id);
  }

  public void rmUserChat(User user){
    user.rmChat(this);
    users.remove(user);
    inboxes.remove(getInboxUser(user));
  }

  public String getId(){
    return id;
  }
}

class Inbox{
  public User user;
  public ArrayList<Msg> msgs = new ArrayList<Msg>();

  public Inbox(User user){
    this.user = user;
  }

  public User getUser(){
    return user;
  }

  public void addMsg(String userId, String msg){
    msgs.add(new Msg(userId, msg));
  }

  public ArrayList<Msg> getMsgs(){
    return msgs;
  }
}

class Msg{
  public String userId;
  public String text;

  public Msg(String userId, String msg){
    this.userId = userId;
    text = msg;
  }
}

public class Solver {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    WhatsappService zap = new WhatsappService();
    while(true){
      String line = scanner.nextLine();
      //System.out.println("$" + line);
      String[] ui = line.split(" ");
      if(ui[0].equals("end")){
        break;
      }else if(ui[0].equals("addUser")){
        zap.createUser(ui[1]);
      }else if(ui[0].equals("allUsers")){
        System.out.println(zap.allUsers());
      }else if(ui[0].equals("newChat")){
        zap.createChat(ui[1], ui[2]);
      }else if(ui[0].equals("chats")){
        System.out.println(zap.getChatsUser(ui[1]));
      }else if(ui[0].equals("invite")){
        zap.addByInvite(ui[1], ui[2], ui[3]);
      }else if(ui[0].equals("users")){
        zap.getUsersChat(ui[1]);
      }else if(ui[0].equals("leave")){
        zap.removerUserChat(ui[1], ui[2]);
      }else if(ui[0].equals("zap")){
        StringBuilder msg = new StringBuilder();
        for(int i = 3; i < ui.length; i++){
          msg.append(ui[i]+" ");
        }
        zap.sendMessage(ui[1], ui[2], msg.toString());
      }else if(ui[0].equals("notify")){
        zap.getNotifyUser(ui[1]);
      }else if(ui[0].equals("ler")){
        zap.readMessageUserChat(ui[1], ui[2]);
      }else{
        System.out.println("fail: comando invalido");
      }
    }
  }
}