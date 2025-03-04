package cn.bugstack.mall.product.service.impl;

import cn.bugstack.mall.product.service.CategoryBrandRelationService;
import cn.bugstack.mall.product.vo.CateLog2VO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.CategoryDao;
import cn.bugstack.mall.product.entity.CategoryEntity;
import cn.bugstack.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //private Map<String,Object> cache = new HashMap<>();

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 2. 找到所有的一级分类
        List<CategoryEntity> level1MenuList = categoryEntityList.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildrenList(menu, categoryEntityList));
                    return menu;
                }).sorted(
                        (menu1, menu2) -> {
                            // 菜单排序
                            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                        }
                ).collect(Collectors.toList());

        return level1MenuList;
    }

    @Override
    public Boolean removeMenuByIds(List<Long> list) {
        // todo 1。检查当前删除的菜单，是否被其他地方引用
        baseMapper.deleteBatchIds(list);
        return true;
    }

    @Override
    public Long[] findCateLogPath(Long cateLogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(cateLogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * @CacheEvict：失效模式
     */
    //@CacheEvict(value = {"category"}, allEntries = true) // 删除这个分区下的所有数据
    @Caching(evict = {
            @CacheEvict(value = {"category"}, key = "'getLevel1Categorys'"),
            @CacheEvict(value = {"category"}, key = "'getCatalogJson'")
    })
    @CachePut // 双写模式，又返回值的数据将修改后的数据再次写入缓存中
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 1.每一个需要缓存的数据我们都来指定要放到哪个名字的缓存【缓存的分区（按照业务类型）】
     *
     * @Cacheable(value = {"category"},key = "#root.method.name") // 代表当前的结果需要缓存，如果缓存中有，则直接从缓存中获取，如果没有，则从数据库中查询，并放入缓存中
     * @Cacheable(value = {"category"},key = "'level1Categorys'") // 指定字符串的写法
     * 2.默认行为：
     * 1）如果缓存中有，方法不用调用
     * 2）key默默人自动生成，缓存的名字::SimpleKeyGenerator.generateKey()
     * 3) 缓存的value的值。默认使用jdk序列化机制，将序列化后的数据保存到redis
     * 4）默认ttl时间 -1
     * 自定义：
     * 1）指定生成的缓存使用的key：key属性置顶，接受一个SpeL 表达式
     * 2）指定缓存的数据的存活时间：配置文件中指定，默认-1（-1代表永久保存）
     * 3）将数据保存为json格式
     * <p>
     * 原理：CacheAutoConfiguration -> RedisCacheConfiguration  -> 自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定用什么配置 ->
     * 如果redisConfiguration配置了，就使用配置的；如果没有配置，就使用默认配置 -> 如果想要更改配置,只需要在容器中放一个RedisCacheConfiguration即可
     * -> 就会应用到当前RedisCacheManager缓存管理的所有缓存分区中
     */
    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    @Cacheable(value = {"category"}, key = "#root.method.name")
    public Map<String, List<CateLog2VO>> getCatalogJson() {
        List<CategoryEntity> categoryAllList = baseMapper.selectList(null);

        // 1。查出所有一级分类
        List<CategoryEntity> categoryEntityList = getParentCid(categoryAllList, 0L);
        // 2. 封装成父子的树形结构
        Map<String, List<CateLog2VO>> resultData = categoryEntityList.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1.每一个一级分类,查到这个一级分类下的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(categoryAllList, v.getCatId());

            List<CateLog2VO> cateLog2VoList = null;
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                cateLog2VoList = categoryEntities.stream().map(level2 -> {
                    CateLog2VO cateLog2VO = new CateLog2VO(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    // 2.2.封装二级分类的三级分类数据
                    List<CategoryEntity> level3CategoryList = getParentCid(categoryAllList, level2.getCatId());
                    if (!CollectionUtils.isEmpty(level3CategoryList)) {
                        List<CateLog2VO.Catalog3VO> catelog3List = level3CategoryList.stream().map(level3 -> {
                            return new CateLog2VO.Catalog3VO(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        cateLog2VO.setCatalog3List(catelog3List);
                    }
                    return cateLog2VO;
                }).collect(Collectors.toList());
            }
            return cateLog2VoList;
        }));

        return resultData;
    }

    /**
     * 查出所有分类，以树形结构组装起来
     * <p>
     * 问题：
     * 会产生堆外内存溢出：OutOfDirectMemoryError
     * springboot2.0默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
     * lettuce的bug导致netty的堆外内存溢出：-Xmx300m ，netty如果没有指定堆外内存，默认使用-Xmx300m
     * 可以通过-Dio.netty.maxDirectMemory进行设置，默认是-Dio.netty.maxDirectMemory=180m
     * 解决方案：
     * ❌不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
     * 1.升级lettuce客户端
     * 2.配置lettuce客户端，使用jedis客户端
     */
    //@Override
    public Map<String, List<CateLog2VO>> getCatalogJson2() {

        /*
         * 1.空结果缓存，解决缓存击穿
         * 2.加锁，解决缓存击穿
         * 3.设置过期时间（加随机值），解决缓存雪崩
         * */

        // 1.加入redis缓存
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");

        if (StringUtils.isEmpty(catalogJson)) {
            // 2.从数据库中查询并封装到redis中
            Map<String, List<CateLog2VO>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }

        // 3.转换为我们需要的对象
        Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<CateLog2VO>>>() {
        });

        return result;
    }

    /**
     * 注意点：缓存与数据库数据一致性问题
     * 1.双写模式
     * 2.失效模式
     *
     * @return
     */
    public Map<String, List<CateLog2VO>> getCatalogJsonFromDbWithRedissonLock() {

        // 1.获取分布式锁
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<CateLog2VO>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return dataFromDb;
    }

    public Map<String, List<CateLog2VO>> getCatalogJsonFromDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(lock)) {
            // 加锁成功，执行业务
            // redisTemplate.expire("lock", 30, TimeUnit.SECONDS); // 注意：加锁与过期时间必须是原子性的
            Map<String, List<CateLog2VO>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                // 解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            // ❌有问题：获取值对比，对比成功删除，原子性操作！lua脚本解锁
            //String lockValue = (String) redisTemplate.opsForValue().get("lock");
            //if (uuid.equals(lockValue)) {
            //    // 删除锁，要判断是不是自己的锁
            //    redisTemplate.delete("lock");
            //}
            return dataFromDb;
        } else {
            // 加锁失败，重试 synchronized
            try {
                Thread.sleep(100, TimeUnit.MILLISECONDS.ordinal());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<CateLog2VO>> getDataFromDb() {
        // 得到锁以后，需要再去缓存中查询一次，如果没有才继续查询
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            // 缓存不为null，直接返回
            Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<CateLog2VO>>>() {
            });
            return result;
        }

        /*将数据库的多次查询变为一次*/
        List<CategoryEntity> categoryAllList = baseMapper.selectList(null);

        // 1。查出所有一级分类
        List<CategoryEntity> categoryEntityList = getParentCid(categoryAllList, 0L);
        // 2. 封装成父子的树形结构
        Map<String, List<CateLog2VO>> resultData = categoryEntityList.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1.每一个一级分类,查到这个一级分类下的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(categoryAllList, v.getCatId());

            List<CateLog2VO> cateLog2VoList = null;
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                cateLog2VoList = categoryEntities.stream().map(level2 -> {
                    CateLog2VO cateLog2VO = new CateLog2VO(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    // 2.2.封装二级分类的三级分类数据
                    List<CategoryEntity> level3CategoryList = getParentCid(categoryAllList, level2.getCatId());
                    if (!CollectionUtils.isEmpty(level3CategoryList)) {
                        List<CateLog2VO.Catalog3VO> catelog3List = level3CategoryList.stream().map(level3 -> {
                            return new CateLog2VO.Catalog3VO(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        cateLog2VO.setCatalog3List(catelog3List);
                    }

                    return cateLog2VO;
                }).collect(Collectors.toList());
            }
            return cateLog2VoList;
        }));

        // 3.查到的数据，再放入缓存，缓存中设置过期时间，解决缓存击穿问题
        stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(resultData), 1, TimeUnit.DAYS);

        return resultData;
    }

    public Map<String, List<CateLog2VO>> getCatalogJsonFromDbWithLocalLock() {

        /*
            // 1.如果缓存中有，直接从缓存中获取
            Map<String, List<CateLog2VO>> catalogJson = (Map<String, List<CateLog2VO>>) cache.get("catalogJson");
            if (cache.get("catalogJson") != null) {
                return catalogJson;
            }
            // 2.缓存中没有，查数据库
            Map<String, List<CateLog2VO>> catalogJsonFromDb = getCatalogJsonFromDb();
            cache.put("catalogJson", catalogJsonFromDb);
        **/

         /*
           // 只要是同一把锁，就能锁住这个锁的所有线程
           synchronized (this) {}：springboot所有的组件在容器中都是单例的
           本地锁：synchronized，JUC（lock），在分布式情况下，想要锁住所有数据，必须使用分布式锁
        **/

        synchronized (this) {
            // 得到锁以后，需要再去缓存中查询一次，如果没有才继续查询
            String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
            if (!StringUtils.isEmpty(catalogJson)) {
                // 缓存不为null，直接返回
                Map<String, List<CateLog2VO>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<CateLog2VO>>>() {
                });
                return result;
            }
        }

        /*将数据库的多次查询变为一次*/
        List<CategoryEntity> categoryAllList = baseMapper.selectList(null);

        // 1。查出所有一级分类
        List<CategoryEntity> categoryEntityList = getParentCid(categoryAllList, 0L);
        // 2. 封装成父子的树形结构
        Map<String, List<CateLog2VO>> resultData = categoryEntityList.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1.每一个一级分类,查到这个一级分类下的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(categoryAllList, v.getCatId());

            List<CateLog2VO> cateLog2VoList = null;
            if (!CollectionUtils.isEmpty(categoryEntities)) {
                cateLog2VoList = categoryEntities.stream().map(level2 -> {
                    CateLog2VO cateLog2VO = new CateLog2VO(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    // 2.2.封装二级分类的三级分类数据
                    List<CategoryEntity> level3CategoryList = getParentCid(categoryAllList, level2.getCatId());
                    if (!CollectionUtils.isEmpty(level3CategoryList)) {
                        List<CateLog2VO.Catalog3VO> catelog3List = level3CategoryList.stream().map(level3 -> {
                            return new CateLog2VO.Catalog3VO(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        cateLog2VO.setCatalog3List(catelog3List);
                    }

                    return cateLog2VO;
                }).collect(Collectors.toList());
            }
            return cateLog2VoList;
        }));

        // 3.查到的数据，再放入缓存，缓存中设置过期时间，解决缓存击穿问题
        stringRedisTemplate.opsForValue().set("catalogJson", JSON.toJSONString(resultData), 1, TimeUnit.DAYS);

        return resultData;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryAllList, Long parentCid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return categoryAllList.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param cateLogId 当前菜单id
     * @param paths     路径
     * @return 菜单路径
     */
    private List<Long> findParentPath(Long cateLogId, List<Long> paths) {
        paths.add(cateLogId);
        CategoryEntity categoryEntity = getById(cateLogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrenList(CategoryEntity menu, List<CategoryEntity> categoryEntityList) {
        return categoryEntityList.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(menu.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrenList(categoryEntity, categoryEntityList));
            return categoryEntity;
        }).sorted(
                (menu1, menu2) -> {
                    // 菜单排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }
        ).collect(Collectors.toList());
    }

}