package factory;

import junit.framework.TestCase;
import models.WaterLogAggModel;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import service.WaterLogAggMapper;

public class MybatisSessionFactoryTest {

    @Test
    public void getSource()
    {
        SqlSessionFactory streamx = MybatisSessionFactory.getSqlSessionFactory("local");
        SqlSession sqlSession = streamx.openSession();

        WaterLogAggMapper mapper = sqlSession.getMapper(WaterLogAggMapper.class);
        WaterLogAggModel test = mapper.select("1", "test");

        System.out.println(test.toString());
    }
}