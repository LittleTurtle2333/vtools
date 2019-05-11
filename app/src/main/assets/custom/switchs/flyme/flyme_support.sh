#!/system/bin/sh

# ro.product.brand=Meizu
# ro.product.manufacturer=Meizu
# ro.build.flyme.version=7
brand=`getprop ro.product.brand`
manufacturer=`getprop ro.product.manufacturer`
version=`getprop ro.build.flyme.version`

if [[ "$version" == "6" ]] && [[ "$brand" == "Meizu" ]] && [[ "$manufacturer" == "Meizu" ]]
then
    echo 1
else
    echo 0
fi
