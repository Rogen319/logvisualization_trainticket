# logvisualization_trainticket

#F12
#Fault Reproduce:
1. Admin Login docker ps -a
2. Lock shanghai and nanjing
3. Login 
4. Cancel ticket. (Open chrome and see the network console)
5. Sometimes you will receive nothing.
6. You will see the log like exception.PNG.
7. Find out why.

#Tips
1. The info in ts-order-other-service/getStatusDescription may be helpful. Of course may be not.
2. You may need the Zipkin Span Log in OrderOtherApplication to help you. Or may be not.