package lk.ijse.dep.app.business.custom.impl;

import lk.ijse.dep.app.business.Converter;
import lk.ijse.dep.app.business.custom.ManageOrdersBO;
import lk.ijse.dep.app.dao.custom.*;
import lk.ijse.dep.app.dto.OrderDTO;
import lk.ijse.dep.app.dto.OrderDTO2;
import lk.ijse.dep.app.dto.OrderDetailDTO;
import lk.ijse.dep.app.entity.CustomEntity;
import lk.ijse.dep.app.entity.Item;
import lk.ijse.dep.app.entity.Order;
import lk.ijse.dep.app.entity.OrderDetail;
import lk.ijse.dep.app.util.JPAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ManageOrdersBOImpl implements ManageOrdersBO {
    private OrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private ItemDAO itemDAO;
    private QueryDAO queryDAO;
    private CustomerDAO customerDAO;


    @Autowired
    public ManageOrdersBOImpl(OrderDAO orderDAO) {

        this.orderDAO =orderDAO;
        //orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
        ///orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
        //itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
        //queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
        //customerDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.CUSTOMER);

    }

    @Override
    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            queryDAO.setEntityManager(em);
            em.getTransaction().begin();

            List<OrderDTO2> orderDTO2s;
            orderDTO2s = queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
               return Converter.getDTOList(ce, OrderDTO2.class);
            }).get();

            em.getTransaction().commit();
            return orderDTO2s;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        }

    }

    @Override
    public List<OrderDTO> getOrders() throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            orderDAO.setEntityManager(em);
            em.getTransaction().begin();
            List<OrderDTO> orderDTOS = orderDAO.findAll().map(Converter::<OrderDTO>getDTOList).get();
            em.getTransaction().commit();
            return orderDTOS;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public String generateOrderId() throws Exception {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            orderDAO.setEntityManager(em);
            em.getTransaction().begin();
            String count = orderDAO.count() + 1 + "";
            em.getTransaction().commit();
            return count;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        }
    }

    @Override
    public void createOrder(OrderDTO dto) throws Exception {
        boolean result=false;
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            orderDAO.setEntityManager(em);
            em.getTransaction().begin();
            orderDAO.save(new Order(dto.getId(), Date.valueOf(dto.getDate()), Converter.getEntity(dto.getCustomerDTO())));
            result = true;
            if (result) {
                orderDetailDAO.setEntityManager(em);

                for (OrderDetailDTO detailDTO : dto.getOrderDetailDTOS()) {
                    orderDetailDAO.save(new OrderDetail(dto.getId(),
                            detailDTO.getCode(), detailDTO.getQty(), detailDTO.getUnitPrice()));


                    itemDAO.setEntityManager(em);
                    Item item = itemDAO.find(detailDTO.getCode()).get();
                    int qty = item.getQtyOnHand() - detailDTO.getQty();
                    item.setQtyOnHand(qty);
                    itemDAO.update(item);

                }
            }

            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        }

    }

    @Override
    public OrderDTO findOrder(String orderId) throws Exception {
        return null;
    }

//    private OrderDAO orderDAO;
//    private OrderDetailDAO orderDetailDAO;
//    private ItemDAO itemDAO;
//    private QueryDAO queryDAO;
//
//    public ManageOrdersBOImpl() {
//        orderDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER);
//        orderDetailDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ORDER_DETAIL);
//        itemDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.ITEM);
//        queryDAO = DAOFactory.getInstance().getDAO(DAOFactory.DAOTypes.QUERY);
//    }
//
//    public List<OrderDTO2> getOrdersWithCustomerNamesAndTotals() throws Exception {
//
//        return queryDAO.findAllOrdersWithCustomerNameAndTotal().map(ce -> {
//            return Converter.getDTOList(ce, OrderDTO2.class);
//        }).get();
//
//    }
//
//    public List<OrderDTO> getOrders() throws Exception {
//
//        List<Order> orders = orderDAO.findAll().get();
//        ArrayList<OrderDTO> tmpDTOs = new ArrayList<>();
//
//        for (Order order : orders) {
//            List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//                return Converter.getDTOList(ce, OrderDetailDTO.class);
//            }).get();
//
//            OrderDTO dto = new OrderDTO(order.getId(),
//                    order.getDate().toLocalDate(),
//                    order.getCustomerId(), tmpOrderDetailsDtos);
//            tmpDTOs.add(dto);
//        }
//
//        return tmpDTOs;
//    }
//
//    public String generateOrderId() throws Exception {
//        return orderDAO.count() + 1 + "";
//    }
//
//    public void createOrder(OrderDTO dto) throws Exception {
//
//        DBConnection.getConnection().setAutoCommit(false);
//
//        try {
//
//            boolean result = orderDAO.save(new Order(dto.getId(), Date.valueOf(dto.getDate()), dto.getCustomerId()));
//
//            if (!result) {
//                return;
//            }
//
//            for (OrderDetailDTO detailDTO : dto.getOrderDetailDTOS()) {
//                result = orderDetailDAO.save(new OrderDetail(dto.getId(),
//                        detailDTO.getCode(), detailDTO.getQty(), detailDTO.getUnitPrice()));
//
//                if (!result) {
//                    DBConnection.getConnection().rollback();
//                    return;
//                }
//
//                Item item = itemDAO.find(detailDTO.getCode()).get();
//                int qty = item.getQtyOnHand() - detailDTO.getQty();
//                item.setQtyOnHand(qty);
//                itemDAO.update(item);
//
//            }
//
//            DBConnection.getConnection().commit();
//
//        } catch (Exception ex) {
//            DBConnection.getConnection().rollback();
//            ex.printStackTrace();
//        } finally {
//            DBConnection.getConnection().setAutoCommit(true);
//        }
//
//    }
//
//    public OrderDTO findOrder(String orderId) throws Exception {
//        Order order = orderDAO.find(orderId).get();
//
//        List<OrderDetailDTO> tmpOrderDetailsDtos = queryDAO.findOrderDetailsWithItemDescriptions(order.getId()).map(ce -> {
//            return Converter.getDTOList(ce, OrderDetailDTO.class);
//        }).get();
//
//        return new OrderDTO(order.getId(), order.getDate().toLocalDate(), order.getCustomerId(), tmpOrderDetailsDtos);
//    }
}
