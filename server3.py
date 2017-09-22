import socket
import sys
from _thread import *
import http.client, urllib.request, urllib.parse, urllib.error, base64
import json

HOST = ''   # Symbolic name meaning all available interfaces
PORT = 8888 # Arbitrary non-privileged port


s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print ("Socket created")

#Bind socket to local host and port
try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print ('Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1])
    sys.exit()

print ('Socket bind complete')

#Start listening on socket
s.listen(10)
print ('Socket now listening')

#Function for handling connections. This will be used to create threads
def clientthread(conn):
   
    #infinite loop so that function do not terminate and thread do not end.
    def get_strongest_emotion(raw_result):
    
        num_faces, res = len(raw_result), raw_result
        if num_faces < 1:
            return None
        elif num_faces == 1:
            return max(res[0]['scores'], key=(lambda s: res[0]['scores'][s]))
        else:
            return [max(face['scores'], key=(lambda s: face['scores'][s])) for face in res]
    
    
    
    def get_emotion(body):
        headers = {
            # Request headers
            'Content-Type': 'application/json',
            'Ocp-Apim-Subscription-Key': '64ee760bdf284e36a9dd050e8dac8733',
        }
    
        params = urllib.parse.urlencode({
        })
    
        try:
            connApi = http.client.HTTPSConnection('westus.api.cognitive.microsoft.com')
            connApi.request("POST", "/emotion/v1.0/recognize?%s" % params, "{ \"url\": \""+body+"\" }", headers)
            response = connApi.getresponse()
            data = response.read().decode()
            print(json.loads(data))
            
            strongest_emotion = get_strongest_emotion(json.loads(data))
            print (strongest_emotion)
            conn.send(json.dumps(data).encode())
            

            connApi.close()
        except Exception as e:
            print("[Errno {0}] {1}".format(e.errno, e.strerror))    

    while True:

        #Receiving from client
        data = conn.recv(1024).decode()
        if not data: 
            break
        print ("from connected  user: " + str(data))
        get_emotion(data)


    #came out of loop
    conn.close()
    

#now keep talking with the client
while 1:
    #wait to accept a connection - blocking call
    conn, addr = s.accept()
    print ('Connected with ' + addr[0] + ':' + str(addr[1]))

    #start new thread takes 1st argument as a function name to be run, second is the tuple of arguments to the function.
    start_new_thread(clientthread ,(conn,))

s.close()


