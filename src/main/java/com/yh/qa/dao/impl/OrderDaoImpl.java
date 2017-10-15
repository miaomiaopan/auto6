package com.yh.qa.dao.impl;

import com.yh.qa.entity.OrderDb;
import com.yh.qa.entity.OutStockOrderInfo;
import com.yh.qa.util.RandomString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.yh.qa.dao.OrderDao;
import com.yh.qa.datasource.DataSourceTemplete;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("OrderDao")
public class OrderDaoImpl implements OrderDao {
	@Autowired
	@Qualifier(DataSourceTemplete.ORDER_DB)
	private JdbcTemplate jdbcTemplateOrderDb;

	@Autowired
	@Qualifier(DataSourceTemplete.PICKLIST_DB)
	private JdbcTemplate jdbcTemplatePickListDb;

	@Autowired
	@Qualifier(DataSourceTemplete.PARTNER_DB)
	private JdbcTemplate jdbcTemplatePartnerDb;

	@Autowired
	@Qualifier(DataSourceTemplete.PICKLIST_DB)
	private JdbcTemplate getJdbcTemplatePickListDb;

	@Override
	public void deleteById(String orderId) {
		jdbcTemplateOrderDb.update("delete from t_trade_order_header where id = ?", orderId);
		jdbcTemplateOrderDb.update("delete from t_trade_order_item where id = ?", orderId);
		jdbcTemplateOrderDb.update("delete from t_trade_order_item_promotion where id = ?", orderId);
	}

	@Override
	public List<OrderDb> getOutStockOrderForChildByParentOrder(String parentOrderId) {
		String sql = "SELECT t3.outstock_order_id,t3.parent_order_id,t3.child_order_id,t4.goods_id,t4.s_sku_code from (SELECT t1.id AS outstock_order_id,t2.parent_order_id,t2.id AS child_order_id FROM t_trade_outstock t1, t_trade_order_header t2 WHERE t1.order_id = t2.id AND t2.parent_order_id = '"
				+ parentOrderId + "') as t3 ,t_trade_order_item t4 WHERE t3.child_order_id = t4.order_id;";

		return (List<OrderDb>) jdbcTemplateOrderDb.query(sql, new RowMapper<OrderDb>() {
			public OrderDb mapRow(ResultSet rs, int rowNum) throws SQLException {
				OrderDb orderDb = new OrderDb();
				orderDb.setOutStockOrderId(rs.getString("outstock_order_id"));
				orderDb.setParentOrderId(rs.getString("parent_order_id"));
				orderDb.setChildOrderId(rs.getString("child_order_id"));
				orderDb.setGoodsId(rs.getString("goods_id"));
				orderDb.setSkuCode(rs.getString("s_sku_code"));
				return orderDb;
			}
		});
	}

	@Override
	public OutStockOrderInfo getOutStockOrderInfo(String orderId) {
		String sql = "select t1.order_id, t1.s_sku_code, t1.sale_price,t1.qty,t2.id as develiry_id, t2.develiry_code from t_trade_order_item t1, t_trade_outstock t2 where t1.order_id = t2.order_id and t2.order_id= ?";
		return jdbcTemplateOrderDb.queryForObject(sql, new Object[] { orderId }, new RowMapper<OutStockOrderInfo>() {
			@Override
			public OutStockOrderInfo mapRow(ResultSet rs, int i) throws SQLException {
				OutStockOrderInfo info = new OutStockOrderInfo();
				info.setOrderId(rs.getString("order_id"));
				info.setSkuCode(rs.getString("s_sku_code"));
				info.setSalePrice(rs.getDouble("sale_price"));
				info.setQty(rs.getDouble("qty"));
				info.setDeveliryId(rs.getString("develiry_id"));
				info.setDeveliryCode(rs.getString("develiry_code"));
				System.out.println(info);
				return info;
			}
		});
	}

	@Override
	public List<String> getBatchIdsByOrderIds(List<String> orderIds) {
		return jdbcTemplatePartnerDb
				.queryForList("SELECT distinct(batch_id) FROM partner_db.t_order_batch where order_id in "
						+ RandomString.getInStrFormList(orderIds), String.class);
	}

	@Override
	public List<String> getOrderIdsByPickWaveids(List<String> waveids) {
		return jdbcTemplatePickListDb.queryForList("select order_id from t_trade_picklist_order where group_id in "
				+ RandomString.getInStrFormList(waveids), String.class);
	}

	@Override
	public Map<String, List<String>> getMapByBatchIds(List<String> batchIds) {
		Map<String, List<String>> batchInfo = new HashMap<String, List<String>>();
		List<String> orderIds = null;
		for (String batchId : batchIds) {
			orderIds = jdbcTemplatePartnerDb.queryForList(
					"SELECT order_id FROM partner_db.t_order_batch where batch_id =" + batchId, String.class);
			batchInfo.put(batchId, orderIds);
		}

		return batchInfo;
	}

	@Override
	public List<String> getSplitInfoByParentsOrderID(List<String> orderIds) {
		List<String> childrenOrder = null;

		for (String orderId : orderIds) {
			childrenOrder = jdbcTemplateOrderDb
					.queryForList("SELECT order_status FROM t_trade_order_header where id =" + orderId, String.class);
		}
		return childrenOrder;
	}

	@Override
	public String getItemIdByOrderIdAndSku(String orderId, String sku) {
		return jdbcTemplateOrderDb.queryForList(
				"SELECT id FROM t_trade_order_item where order_id = '" + orderId + "' and goods_id = '" + sku + "'",
				String.class).get(0);
	}

	@Override
	public String getServiceOrderIdByOrderId(String orderId) {
		return jdbcTemplatePartnerDb.queryForList(
				"SELECT service_order_id FROM partner_db.t_service_order where order_id = '" + orderId + "'",
				String.class).get(0);
	}

	@Override
	public List<String> getSubOrderIdByParentOrderID(String orderId) {
		return jdbcTemplateOrderDb
				.queryForList("SELECT children_order_ids FROM t_trade_order_header where id =" + orderId, String.class);

	}

	@Override
	public String getStatusByOrderId(String orderId) {
		return (String) jdbcTemplateOrderDb
				.queryForObject("SELECT order_status FROM t_trade_order_header where id =" + orderId, String.class);
	}

	@Override
	public String getWaveIdByOrderId(String orderId) {
		String sql = "SELECT g.id FROM t_trade_picklist_group g where g.id= (SELECT o.group_id FROM t_trade_picklist_order o where o.order_id=? and o.pick_mode ='group');";
		return jdbcTemplatePickListDb.queryForObject(sql,new Object[]{orderId},String.class);
	}

}
