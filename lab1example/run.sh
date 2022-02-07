for i in `seq 0 2`; do
  ./estarter $i &
done

trap_ctrlc() {
    echo "Closing servers..."
    pkill -9 -f "java mcgui.Main"
}

trap trap_ctrlc INT

wait