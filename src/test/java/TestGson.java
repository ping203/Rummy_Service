import com.athena.services.config.LoadConfigProperties;
import com.athena.services.impl.auth.AuthServiceImpl;

public class TestGson {
    public static void main(String[] args) {
        System.out.println(LoadConfigProperties.getConfig("min_vip_hotnew"));

	}

    private static void testAuth(AuthServiceImpl authService) {
        for(int i = 0 ; i < 5; i ++){
            testThread(i);
        }
    }

    private static int testThread(int i){
        try{
            System.out.println("start...0" + i);
            Thread.sleep(2000);
            return i;
        }catch (Exception e){

        }finally {
            System.out.println("finally...");
        }
        return 0;
    }
}
