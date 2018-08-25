# logvisualization_trainticket

#F12

setup system:

1.  * kubectl create -f <(istioctl kube-inject -f ts-deployment-part1.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part2.yml)
    * kubectl create -f <(istioctl kube-inject -f ts-deployment-part3.yml)
    * istioctl create -f trainticket-gateway.yaml
2. Log in and make sure that there is at least one order that fits the following:
   This order must be:  1. The train number is start with Z or K
                        2. The order status is PAID


fault reproduce manually step:

1. Click [Admin Management] and login with admin account
2. Enter "shanghai" and "nanjing" into the two input box at the upper right part of page
3. Click [Search And Lock]
4. Return to the index page and login
5. Click [Flow Two - Ticket Cancel & Ticket Change]
7. Click [Refresh Orders]
8. Select the order mentioned above and click [Cancel Order]
9. You will get Error alert and see the exception logs on the server console.
