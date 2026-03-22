import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
class User {
    String userId;
    double balance;
    ReentrantLock lock = new ReentrantLock();

    User(String userId,double balance){
        this.userId=userId;
        this.balance=balance;
    }

    void credit(double amount){
        lock.lock();
        try{
            balance=balance+amount;
        }finally{
            lock.unlock();
        }
    }

    boolean debit(double amount){
        lock.lock();
        try{
            if(balance>=amount){
                balance=balance-amount;
                return true;
            }
            return false;
        }finally{
            lock.unlock();
        }
    }

    double getBalance(){
        lock.lock();
        try{
            return balance;
        }finally{
            lock.unlock();
        }
    }
}

class WalletService{
    ConcurrentHashMap<String,User> map=new ConcurrentHashMap<>();

    void createUser(String userId,double balance){
        map.putIfAbsent(userId,new User(userId,balance));
    }

    void credit(String userId,double amount){
        User u=map.get(userId);
        if(u!=null){
            u.credit(amount);
        }
    }

    boolean debit(String userId,double amount){
        User u=map.get(userId);
        if(u!=null){
            return u.debit(amount);
        }
        return false;
    }

    double getBalance(String userId){
        User u=map.get(userId);
        if(u!=null){
            return u.getBalance();
        }
        return 0;
    }
}

class DebitThread extends Thread{
    WalletService wallet;
    String userId;
    double amount;

    DebitThread(WalletService wallet,String userId,double amount,String name){
        super(name);
        this.wallet=wallet;
        this.userId=userId;
        this.amount=amount;
    }

    public void run(){
        boolean res=wallet.debit(userId,amount);
        System.out.println(getName()+" debit "+amount+" -> "+(res?"success":"failed"));
    }
}


public class Main{
    public static void main(String[] args) throws Exception{

        WalletService wallet=new WalletService();
        wallet.createUser("u1",100);

        DebitThread t1=new DebitThread(wallet,"u1",50,"T1");
        DebitThread t2=new DebitThread(wallet,"u1",70,"T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("final balance="+wallet.getBalance("u1"));
    }
}