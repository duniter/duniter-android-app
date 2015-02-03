#!/bin/sh
set -e

echo "=== Installing Swig 2.0.10... ==="
wget http://prdownloads.sourceforge.net/swig/swig-2.0.10.tar.gz
tar -xvf swig-2.0.10.tar.gz
cd swig-2.0.10
./configure
make -j 5
sudo make install
cd ..
rm -rf swig-2.0.10
rm swig-2.0.10.tar.gz

echo "=== Installing Swig 2.0.10 [Done] ==="
