from os.path import abspath
from timeit import Timer

import cv2
import numpy as np
import os.path
import cv2
import json
from flask import Flask, request, Response
import uuid
from PIL import Image, ImageChops
import warnings
import glob

warnings.simplefilter("ignore", DeprecationWarning)


# Function detect face from image
def save_user_image(img, name, image_qty):
    # save file
    type=name.lower().replace('"','')
    path_file = ('static/%s.png' % uuid.uuid4().hex)
    cv2.imwrite(path_file, img)
    final_path=type+"_"+image_qty
    path=os.path.join(type,'1.jpg')
    print(path)
    original = cv2.imread(path)
    duplicate = cv2.imread(path_file)
    if original.shape == duplicate.shape:
        print("The images have same size and channels")
        difference = cv2.subtract(original, duplicate)
        b, g, r = cv2.split(difference)
        if cv2.countNonZero(b) == 0 and cv2.countNonZero(g) == 0 and cv2.countNonZero(r) == 0:
            print("The images are completely Equal")
        else:
            print("The images are NOT equal")
    # 2) Check for similarities between the 2 images
    sift = cv2.xfeatures2d.SIFT_create()
    kp_1, desc_1 = sift.detectAndCompute(original, None)
    kp_2, desc_2 = sift.detectAndCompute(duplicate, None)

    index_params = dict(algorithm=0, trees=5)
    search_params = dict()
    flann = cv2.FlannBasedMatcher(index_params, search_params)

    matches = flann.knnMatch(desc_1, desc_2, k=2)

    good_points = []
    for m, n in matches:
        if m.distance < 0.6 * n.distance:
            good_points.append(m)

    # Define how similar they are
    number_keypoints = 0
    if len(kp_1) <= len(kp_2):
        number_keypoints = len(kp_1)
    else:
        number_keypoints = len(kp_2)

    print("Keypoints 1ST Image: " + str(len(kp_1)))
    print("Keypoints 2ND Image: " + str(len(kp_2)))
    print("GOOD Matches:", len(good_points))
    print("How good it's the match: ", len(good_points) / number_keypoints * 100)
    x = {
        "1ST_Image": str(len(kp_1)),
        "2ND_Image": str(len(kp_2)),
        "Good_matches": len(good_points),
        "Result": len(good_points) / number_keypoints * 100
    }
    return json.dumps(x)


# API
app = Flask(__name__)


# route http post to this method
@app.route('/api/upload', methods=['POST'])
def upload():
    # retrive image from client
    img = cv2.imdecode(np.fromstring(request.files['image'].read(), np.uint8), cv2.IMREAD_UNCHANGED)
    name = request.form.get('type', '')
    image_qty = request.form.get('image_qty', '')
    # process image
    img_processed = save_user_image(img,name, image_qty)
    # response
    return Response(response=img_processed, status=200, mimetype="application/json")  # return json


# start server
app.run(host="192.168.43.129", port=5000)
