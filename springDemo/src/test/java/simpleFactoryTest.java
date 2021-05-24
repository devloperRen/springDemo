import pattern.Factoy.simpleFactory.CreateHumanFactory;
import pattern.Factoy.IHuman;
import pattern.Factoy.Male;

public class simpleFactoryTest {

    public static void main(String[] args) {
            //直接调用工厂生产人类对象，这里要不要考虑
        CreateHumanFactory createHumanFactory = new CreateHumanFactory();
        IHuman iHuman = createHumanFactory.createHuman(Male.class);
        iHuman.say();
    }
}
