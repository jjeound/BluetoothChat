package com.gogumac.bluetoothchat.bluetooth

/**
 * 📌 블루투스 상태를 관리하는 `enum class`
 * - 블루투스 연결, 검색, 페어링 등 다양한 상태를 관리하는 **상태값 집합**
 * - `BluetoothService`에서 **현재 블루투스 상태를 추적하는 데 사용됨**
 */
enum class BluetoothState {

    /**
     * ✅ `STATE_DISABLE`
     * - **블루투스 기능이 비활성화됨**
     * - 블루투스가 꺼져 있어서 사용할 수 없는 상태
     * - `BluetoothAdapter.isEnabled == false` 일 때 해당 상태가 됨
     */
    STATE_DISABLE,

    /**
     * ✅ `STATE_NONE`
     * - **아직 블루투스 작업이 수행되지 않음**
     * - 초기 상태이며, 블루투스가 활성화되어 있지만 **검색, 연결 등이 진행되지 않은 상태**
     */
    STATE_NONE,

    /**
     * ✅ `STATE_DISCOVERING`
     * - **블루투스 기기 검색이 진행 중인 상태**
     * - `BluetoothAdapter.startDiscovery()` 가 호출되면 해당 상태로 변경됨
     */
    STATE_DISCOVERING,

    /**
     * ✅ `STATE_DISCOVERING_FINISHED`
     * - **블루투스 검색이 완료된 상태**
     * - `BluetoothAdapter.ACTION_DISCOVERY_FINISHED` 이벤트가 발생하면 해당 상태가 됨
     */
    STATE_DISCOVERING_FINISHED,

    /**
     * ✅ `STATE_START_BOND`
     * - **새로운 블루투스 장치와 페어링 요청을 시작한 상태**
     * - `BluetoothDevice.createBond()` 가 호출되면 이 상태로 변경됨
     */
    STATE_START_BOND,

    /**
     * ✅ `STATE_BONDING`
     * - **블루투스 페어링이 진행 중인 상태**
     * - `BluetoothDevice.ACTION_BOND_STATE_CHANGED` 이벤트가 발생하면 해당 상태로 변경됨
     */
    STATE_BONDING,

    /**
     * ✅ `STATE_BONDED`
     * - **블루투스 페어링이 성공적으로 완료된 상태**
     * - `BluetoothDevice.BOND_BONDED` 상태로 변경되면 해당 상태가 됨
     */
    STATE_BONDED,

    /**
     * ✅ `STATE_BONDING_REJECTED`
     * - **사용자가 블루투스 페어링 요청을 거부한 상태**
     * - 기기가 페어링을 시도했지만, 상대 장치에서 거절한 경우 이 상태가 됨
     */
    STATE_BONDING_REJECTED,

    /**
     * ✅ `STATE_OPEN_SERVER_SOCKET`
     * - **블루투스 서버 소켓을 열어 클라이언트 연결을 기다리는 상태**
     * - `bluetoothAdapter.listenUsingRfcommWithServiceRecord()` 호출 시 해당 상태가 됨
     */
    STATE_OPEN_SERVER_SOCKET,

    /**
     * ✅ `STATE_CONNECTING`
     * - **블루투스 기기와 연결을 시도 중인 상태**
     * - `BluetoothSocket.connect()` 호출 시 해당 상태로 변경됨
     */
    STATE_CONNECTING,

    /**
     * ✅ `STATE_CONNECTED`
     * - **블루투스 장치와 성공적으로 연결된 상태**
     * - `BluetoothSocket` 연결이 완료되면 해당 상태가 됨
     */
    STATE_CONNECTED,

    /**
     * ✅ `STATE_CLOSE_CONNECT`
     * - **블루투스 연결이 해제된 상태**
     * - `BluetoothSocket.close()` 가 호출되었거나, 상대 장치에서 연결을 끊었을 때 해당 상태로 변경됨
     */
    STATE_CLOSE_CONNECT;
}
