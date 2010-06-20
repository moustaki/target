from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from django.utils import simplejson

def singleton(cls):
    instance_container = []
    def getinstance():
        if not len(instance_container):
            instance_container.append(cls())
        return instance_container[0]
    return getinstance

# A singleton class wrapping the game state - perhaps worth persisting it
@singleton
class State:
    max_player_id = 0
    players = {}

class GameController(webapp.RequestHandler):
    def get(self, game_id):
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Hello, webapp World!' + str(game_id))

class PlayerController(webapp.RequestHandler):
    def get(self, player_id):
        player_id = int(player_id)
        state = State()
        self.response.headers['Content-Type'] = 'application/json'
        if player_id <= state.max_player_id:
            self.response.out.write(simplejson.dumps(state.players[player_id]))
        else:
            self.response.out.write('{}')
    def post(self, player_id):
        player_id = int(player_id)
        state = State()
        if player_id <= state.max_player_id:
            self.response.headers['Content-Type'] = 'application/json'
            side = int(self.request.get('side'))
            state.players[player_id]['side'] = side
            self.response.out.write(simplejson.dumps(state.players[player_id]))
        else:
            self.response.write('{}')
        

class RegisterController(webapp.RequestHandler):
    def get(self):
        state = State()
        state.max_player_id += 1
        state.players[state.max_player_id] = {"id":state.max_player_id}
        self.response.headers['Content-Type'] = 'application/json'
        self.response.out.write(simplejson.dumps(state.players[state.max_player_id]))

application = webapp.WSGIApplication(
                                     [
                                        ('/games/(\d+)', GameController),
                                        ('/players/(\d+)', PlayerController),
                                        ('/register', RegisterController)
                                     ],
                                     debug=True)

def main():
    run_wsgi_app(application)

if __name__ == "__main__":
    main()