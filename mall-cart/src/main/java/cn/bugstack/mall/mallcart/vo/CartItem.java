package cn.bugstack.mall.mallcart.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description 购物项内容
 * @date 2025/3/15 18:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class CartItem implements Serializable {

    private static final long serialVersionUID = 3413527000289536800L;

    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(final Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(final Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(final String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(final List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(final Integer count) {
        this.count = count;
    }

    /**
     * 计算当前购物项总价
     *
     * @return
     */
    public BigDecimal getTotalPrice() {
        this.totalPrice = this.price.multiply(new BigDecimal(this.count.toString()));
        return totalPrice;
    }

    public void setTotalPrice(final BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
